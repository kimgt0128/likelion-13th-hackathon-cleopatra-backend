package com.likelion.cleopatra.domain.crwal.impl;

import com.likelion.cleopatra.domain.crwal.dto.place.NaverPlaceReview;
import com.likelion.cleopatra.domain.crwal.selector.NaverPlaceSelectors;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitUntilState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class NaverReviewCrawler {

    /** Page를 직접 주입하지 않고 Context 주입 → 메서드 내에서 page 생성/정리 */
    private final BrowserContext context;

    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");
    private static final boolean DEBUG = Boolean.parseBoolean(System.getProperty("crawler.debug", "true"));
    private static final boolean SHOT  = Boolean.parseBoolean(System.getProperty("crawler.shot",  "true"));
    private static final boolean TRACE = Boolean.parseBoolean(System.getProperty("crawler.trace", "true"));

    /** placeLink에서 최대 count개 리뷰 수집 */
    public List<NaverPlaceReview> crawlReviews(String placeLink, int count) {
        List<NaverPlaceReview> out = new ArrayList<>();
        String ts = TS.format(LocalDateTime.now());
        log.info("[NR] start url={} count={}", trimUrl(placeLink), count);

        Page page = context.newPage();

        if (TRACE) {
            try { context.tracing().start(new Tracing.StartOptions().setScreenshots(true).setSnapshots(true).setSources(true)); }
            catch (Throwable ignored) {}
        }

        // 로깅 훅
        if (DEBUG) {
            page.onConsoleMessage(m -> log.debug("[NR/console] {} {}: {}", m.type(), String.valueOf(m.location()), m.text()));
            page.onRequestFailed(r -> log.debug("[NR/req-failed] {} {} cause={}", r.method(), trimUrl(r.url()), r.failure()));
            page.onResponse(res -> { if (res.status() >= 400)
                log.debug("[NR/resp] {} {} status={}", res.request().method(), trimUrl(res.url()), res.status()); });
            page.onFrameAttached(f -> log.debug("[NR/frame+] name={} url={}", f.name(), trimUrl(f.url())));
            page.onFrameNavigated(f -> log.debug("[NR/frame→] name={} url={}", f.name(), trimUrl(f.url())));
        }

        // 리소스 차단(미디어만)
        page.route("**/*", r -> {
            String t = r.request().resourceType();
            if ("media".equals(t)) r.abort(); else r.resume();
        });

        page.setDefaultTimeout(25_000);
        page.setDefaultNavigationTimeout(30_000);

        try {
            page.navigate(placeLink, new Page.NavigateOptions().setWaitUntil(WaitUntilState.LOAD).setTimeout(30_000));
            waitGenerously(page);
            clickIfVisible(page, NaverPlaceSelectors.BANNER_ACCEPT, "[NR] cookie-accept");

            // entry iframe
            Frame entry = waitIframeByIdOrUrl(page, "entryIframe", "/entry", 10_000);
            if (entry == null) {
                log.debug("[NR] entryIframe not found");
                safeShot(page, "nr_no_entry_" + ts);
                return out;
            }
            log.debug("[NR] entryIframe ok url={}", trimUrl(entry.url()));

            // 리뷰 탭 클릭
            Locator tab = entry.locator("span:has-text(\"리뷰\")").first();
            if (tab.count() > 0) {
                try { tab.click(new Locator.ClickOptions().setTimeout(5_000).setForce(true)); }
                catch (Throwable e) { try { tab.evaluate("el=>el.click()"); } catch (Throwable ignored) {} }
                page.waitForTimeout(500);
                log.info("[NR] 리뷰 탭 클릭");
            } else {
                log.debug("[NR] 리뷰 탭 미검출");
            }

            // 리뷰 리스트 대기
            Locator ul = entry.locator("ul#_review_list");
            ul.waitFor(new Locator.WaitForOptions().setTimeout(10_000));
            Locator lis = ul.locator(":scope > li.place_apply_pui");
            log.debug("[NR] 초기 리뷰 수 {}", lis.count());

            // 스크롤로 추가 로드
            int target = Math.max(1, count);
            int attempts = 0, last = -1;
            while (lis.count() < target && attempts++ < 24) {
                try { ul.evaluate("el => el.scrollTop = el.scrollHeight"); } catch (Throwable ignored) {}
                page.waitForTimeout(ThreadLocalRandom.current().nextInt(260, 440));
                int now = lis.count();
                log.debug("[NR] lazy-load attempt={} count={}", attempts, now);
                if (now == last) break;
                last = now;
            }

            int total = lis.count();
            int limit = Math.min(target, total);
            log.info("[NR] parse total={} limit={}", total, limit);
            if (total == 0) safeShot(page, "nr_zero_list_" + ts);

            for (int i = 0; i < limit; i++) {
                String tagIdx = "nr_idx" + i + "_" + ts;
                try {
                    Locator li = lis.nth(i);

                    // 방문 키워드
                    List<String> visitKeywords = cleanTexts(li.locator("a[data-pui-click-code='visitkeywords'] span").allInnerTexts());

                    // 본문(더보기 처리)
                    Locator more = li.locator("a.pui__wFzIYl");
                    if (more.count() > 0 && more.isVisible()) {
                        try { more.click(new Locator.ClickOptions().setTimeout(2_000)); } catch (Throwable ignored) {}
                        page.waitForTimeout(120);
                        log.debug("[NR] #{} 더보기 클릭", i + 1);
                    }
                    String body = normalize(safeInnerText(li.locator("div.pui__vn15t2")));
                    body = body.replace("더보기", "").trim();

                    // 재방문 문구
                    String revisit = li.locator("span:has-text(\"번째 방문\")").first().innerText(new Locator.InnerTextOptions().setTimeout(5000)).trim();
                    if (revisit == null) revisit = "";

                    // 만족 태그 칩
                    List<String> tags = cleanTexts(li.locator("div.pui__HLNvmI :not(a).pui__jhpEyP").allInnerTexts());

                    NaverPlaceReview rv = NaverPlaceReview.builder()
                            .link(placeLink)
                            .visitKeywords(visitKeywords)
                            .body(body)
                            .revisit(revisit)
                            .tags(tags)
                            .build();

                    out.add(rv);
                    log.info("[NR] OK #{}/{} visitKeywords={} revisit='{}' tags={} bodyLen={}",
                            i + 1, limit, visitKeywords.size(), revisit, tags.size(), body.length());

                } catch (Throwable e) {
                    log.debug("[NR] idx{} parse-ex {} msg={}", i, e.getClass().getSimpleName(), e.getMessage());
                    safeShot(page, tagIdx + "_parse_err");
                }
            }

            log.info("[NR] done size={}", out.size());
            return out;

        } catch (Throwable e) {
            log.error("[NR] fail url={} reason={}", trimUrl(placeLink), e.toString(), e);
            return out;
        } finally {
            if (TRACE) {
                try {
                    Path outZip = Path.of("build/trace", "naver_review_" + ts + ".zip");
                    Files.createDirectories(outZip.getParent());
                    context.tracing().stop(new Tracing.StopOptions().setPath(outZip));
                    log.debug("[NR] trace saved {}", outZip.toAbsolutePath());
                } catch (Throwable ignored) {}
            }
            try { page.close(); } catch (Throwable ignored) {}
        }
    }

    /** 기존 시그니처 유지: count번째(1-based) 리뷰 1건 */
    public NaverPlaceReview crawlReview(String placeLink, int count) {
        List<NaverPlaceReview> list = crawlReviews(placeLink, count);
        if (list.isEmpty()) return null;
        int idx = Math.min(count, list.size()) - 1;
        NaverPlaceReview rv = list.get(idx);
        log.info("[NR] single index={} revisit='{}' bodyLen={}", idx, rv.getRevisit(), rv.getBody() == null ? 0 : rv.getBody().length());
        return rv;
    }

    // ---------- helpers (NaverPlaceCrawler와 동일 톤) ----------

    private static void waitGenerously(Page p) {
        try { p.waitForLoadState(LoadState.DOMCONTENTLOADED); } catch (Throwable ignored) {}
        try { p.waitForLoadState(LoadState.LOAD); } catch (Throwable ignored) {}
        try { p.waitForLoadState(LoadState.NETWORKIDLE); } catch (Throwable ignored) {}
        try {
            p.waitForFunction("() => document.readyState==='complete' || document.querySelector('iframe#entryIframe')",
                    new Page.WaitForFunctionOptions().setTimeout(8_000));
        } catch (Throwable ignored) {}
        p.waitForTimeout(ThreadLocalRandom.current().nextInt(900, 1600));
    }

    private static Frame waitIframeByIdOrUrl(Page p, String id, String urlSub, int timeoutMs) {
        long end = System.currentTimeMillis() + Math.max(1000, timeoutMs);
        while (System.currentTimeMillis() < end) {
            try {
                ElementHandle h = p.querySelector("iframe#" + id);
                if (h != null) {
                    Frame f = h.contentFrame();
                    if (f != null) return f;
                }
            } catch (Throwable ignored) {}
            for (Frame f : p.frames()) {
                try {
                    String u = f.url();
                    String n = f.name();
                    if ((n != null && n.equals(id)) || (u != null && u.contains(urlSub))) return f;
                } catch (Throwable ignored) {}
            }
            p.waitForTimeout(120);
        }
        return null;
    }

    private static void clickIfVisible(Page p, String selector, String tag) {
        try {
            Locator x = p.locator(selector).first();
            if (x.count() > 0 && x.isVisible()) {
                x.click(new Locator.ClickOptions().setTimeout(1500));
                log.debug("{} clicked selector={}", tag, selector);
            }
        } catch (Throwable ignored) {}
    }

    private static String safeInnerText(Locator locator) {
        try { return locator.innerText(); } catch (Throwable t) { return ""; }
    }

    private static String normalize(String s) {
        if (s == null) return "";
        return s.replace('\u00A0', ' ')
                .replaceAll("[ \\t\\x0B\\f\\r]+", " ")
                .replaceAll("\\n{2,}", "\n")
                .trim();
    }

    private static List<String> cleanTexts(List<String> list) {
        if (list == null) return List.of();
        return list.stream().map(NaverReviewCrawler::normalize).filter(s -> !s.isBlank()).collect(Collectors.toList());
    }

    private static String trimUrl(String u) {
        if (u == null) return null;
        if (u.length() <= 140) return u;
        int q = u.indexOf('?');
        return q > 0 ? u.substring(0, Math.min(q, 140)) + "..." : u.substring(0, 140) + "...";
    }

    private static void safeShot(Page p, String name) {
        if (!SHOT) return;
        try {
            Path dir = Path.of("build", "screenshots");
            Files.createDirectories(dir);
            Path path = dir.resolve(name + ".png");
            p.screenshot(new Page.ScreenshotOptions().setPath(path).setFullPage(true));
            log.debug("[NR] screenshot {}", path.toAbsolutePath());
        } catch (Throwable ignored) {}
    }
}
