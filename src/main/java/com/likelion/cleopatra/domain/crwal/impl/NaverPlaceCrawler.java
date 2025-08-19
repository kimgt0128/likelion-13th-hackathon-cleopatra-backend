// src/main/java/com/likelion/cleopatra/domain/crwal/impl/NaverPlaceCrawler.java
package com.likelion.cleopatra.domain.crwal.impl;

import com.likelion.cleopatra.domain.crwal.dto.place.NaverPlaceLinkRes;
import com.likelion.cleopatra.domain.crwal.selector.NaverPlaceSelectors;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitUntilState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class NaverPlaceCrawler {
    private final BrowserContext context;

    private static final Pattern PID_ENTRY = Pattern.compile("/entry/place/(\\d+)");
    private static final Pattern PID_RESTAURANT = Pattern.compile("/restaurant/(\\d+)");
    private static final Pattern PID_QUERY = Pattern.compile("[?&]placeId=(\\d+)");
    private static final Pattern PID_PLACE = Pattern.compile("/place/(\\d+)(?=[/?#]|$)");
    private static final DateTimeFormatter TS = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS");

    // 시스템 프로퍼티 토글
    private static final boolean DEBUG = Boolean.parseBoolean(System.getProperty("crawler.debug", "true"));
    private static final boolean SHOT  = Boolean.parseBoolean(System.getProperty("crawler.shot",  "true"));
    private static final boolean TRACE = Boolean.parseBoolean(System.getProperty("crawler.trace", "true"));

    public List<NaverPlaceLinkRes> crawlLinks(String keyword, int placeLimit) {
        String enc = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
        String pSearch = "https://map.naver.com/p/search/" + enc + "?c=14.00,0,0,0,dh";
        String v5Search = "https://map.naver.com/v5/search/" + enc;
        String pcmapList = "https://pcmap.place.naver.com/place/list?query=" + enc;

        Page page = context.newPage();

        if (TRACE) {
            try {
                context.tracing().start(new Tracing.StartOptions()
                        .setScreenshots(true).setSnapshots(true).setSources(true));
            } catch (Throwable ignored) {}
        }

        // 이벤트 훅: 실패 원인 가시화
        if (DEBUG) {
            page.onConsoleMessage(m ->
                    {  String loc = String.valueOf(m.location()); // 버전차 처리: String이면 그대로, 객체면 toString()
                        log.debug("[NP/console] {} {}: {}", m.type(), trimUrl(loc), m.text());
                    });
            page.onRequestFailed(r ->
                    log.debug("[NP/req-failed] {} {} cause={}", r.method(), trimUrl(r.url()), r.failure()));
            page.onResponse(res -> {
                int s = res.status();
                if (s >= 400) {
                    log.debug("[NP/resp] {} {} status={} bodyPreview={}",
                            res.request().method(), trimUrl(res.url()), s, safePreview(res.text(), 200));
                }
            });
            page.onFrameAttached(f -> log.debug("[NP/frame+] name={} url={}", f.name(), trimUrl(f.url())));
            page.onFrameNavigated(f -> log.debug("[NP/frame→] name={} url={}", f.name(), trimUrl(f.url())));
        }

        // 리소스 차단 최소화: media만
        page.route("**/*", r -> {
            String t = r.request().resourceType();
            if ("media".equals(t)) r.abort();
            else r.resume();
        });

        page.setDefaultTimeout(25_000);
        page.setDefaultNavigationTimeout(30_000);
        List<NaverPlaceLinkRes> out = new ArrayList<>();

        String ts = TS.format(LocalDateTime.now());

        try {
            // 기본 정보 로깅
            String ua = String.valueOf(page.evaluate("() => navigator.userAgent"));
            var vs = page.viewportSize();
            log.debug("[NP] start kw='{}' limit={} url={} ua='{}' viewport={}",
                    keyword, placeLimit, pSearch, ua, (vs == null ? "null" : vs.width + "x" + vs.height));

            page.navigate(pSearch, new Page.NavigateOptions().setTimeout(30_000).setWaitUntil(WaitUntilState.LOAD));
            waitGenerously(page);
            clickIfVisible(page, NaverPlaceSelectors.BANNER_ACCEPT, "[NP] cookie-accept");

            ensureDesktop(page);
            dumpFrames(page, "[NP] after /p/search");

            Frame searchFrame = waitIframeByIdOrUrl(page, "searchIframe", "/search", 9_000);

            if (searchFrame == null) {
                log.debug("[NP] searchIframe miss → v5");
                safeShot(page, "miss1_" + ts);
                page.navigate(v5Search, new Page.NavigateOptions().setTimeout(30_000).setWaitUntil(WaitUntilState.LOAD));
                waitGenerously(page);
                clickIfVisible(page, NaverPlaceSelectors.BANNER_ACCEPT, "[NP] cookie-accept");
                ensureDesktop(page);
                dumpFrames(page, "[NP] after /v5/search");
                searchFrame = waitIframeByIdOrUrl(page, "searchIframe", "/search", 9_000);
            }
            if (searchFrame == null) {
                log.debug("[NP] searchIframe miss2 → pcmap list");
                safeShot(page, "miss2_" + ts);
                page.navigate(pcmapList, new Page.NavigateOptions().setTimeout(30_000).setWaitUntil(WaitUntilState.LOAD));
                waitGenerously(page);
                clickIfVisible(page, NaverPlaceSelectors.BANNER_ACCEPT, "[NP] cookie-accept");
                ensureDesktop(page);
                dumpFrames(page, "[NP] after /place/list");
                searchFrame = waitIframeByIdOrUrl(page, "searchIframe", "/place/list", 9_000);
            }

            boolean frameLess = (searchFrame == null);
            if (frameLess) log.debug("[NP] frameless mode. fallback to top DOM.");

            int limit = Math.max(1, placeLimit);
            Locator items;

            if (!frameLess) {
                lazyScrollInFrame(searchFrame, NaverPlaceSelectors.SEARCH_LIST_CONTAINER, rand(5, 8));
                items = searchFrame.locator(NaverPlaceSelectors.SEARCH_LIST_ITEMS);
                if (items.count() == 0) {
                    log.debug("[NP] list 0 by CSS. try XPath fallback");
                    items = searchFrame.locator("xpath=/html/body/div[3]/div/div[2]/div[1]/ul/li");
                }
            } else {
                items = page.locator(
                        "#_pcmap_list_scroll_container ul > li, " +
                                "ul._3jtfG > li, ul.RgCez > li, div._1xg8B > ul > li"
                );
                if (items.count() == 0) {
                    log.debug("[NP] top list 0. try scroll prime then requery.");
                    for (int k = 0; k < 6; k++) { page.evaluate("() => window.scrollBy(0, 900)"); page.waitForTimeout(150); }
                    items = page.locator("#_pcmap_list_scroll_container ul > li");
                }
            }

            int available = items.count();
            int target = Math.min(available, limit);
            log.debug("[NP] list count={} target={} frameless={}", available, target, frameLess);
            if (available == 0) safeShot(page, "no-list_" + ts);

            for (int i = 0; i < target; i++) {
                String stepTag = "idx" + i + "_" + ts;
                try {
                    Locator li = items.nth(i);
                    Locator link = li.locator(NaverPlaceSelectors.SEARCH_CARD_LINK).first();

                    int linkCnt = link.count();
                    if (linkCnt == 0) {
                        log.debug("[NP] idx={} no link by selector={} → skip", i, NaverPlaceSelectors.SEARCH_CARD_LINK);
                        safeShot(page, stepTag + "_nolink");
                        continue;
                    }

                    String name = safe(textOrEmpty(li.locator("span.TYaxT, span.YwYLL").first()));
                    if (name.isBlank()) name = safe(textOrEmpty(link));
                    log.debug("[NP] idx={} name='{}'", i, name);

                    boolean jsClicked = tryJsClick(link);
                    if (!jsClicked) {
                        link.click(new Locator.ClickOptions().setForce(true).setTimeout(12_000));
                        log.debug("[NP] idx={} JS click fail → force-click", i);
                    }
                    page.waitForTimeout(800);

                    String topUrl = page.url();
                    String pidTop = extractPlaceId(topUrl);
                    Frame entry = waitIframeByIdOrUrl(page, "entryIframe", "/entry", 6_000);
                    String entryUrl = entry != null ? entry.url() : null;
                    String pidEntry = extractPlaceId(entryUrl);

                    String pid = pidTop != null ? pidTop : pidEntry;
                    if (pid == null) {
                        log.debug("[NP] idx={} placeId null. topUrl={} entryUrl={}", i, trimUrl(topUrl), trimUrl(entryUrl));
                        safeShot(page, stepTag + "_no_pid");
                        continue;
                    }

                    String canonical = "https://map.naver.com/p/search/" + enc + "/place/" + pid;
                    out.add(NaverPlaceLinkRes.builder()
                            .placeId(pid)
                            .placeName(name)
                            .placeUrl(canonical)
                            .build());

                    log.debug("[NP] idx={} OK pid={} canonical={}", i, pid, canonical);
                    jitter(page, 120, 240);
                } catch (Exception e) {
                    log.debug("[NP] idx={} exception={} msg={}", i, e.getClass().getSimpleName(), e.getMessage());
                    safeShot(page, stepTag + "_exception");
                }
            }

            log.debug("[NP] done count={}", out.size());
            return out;

        } finally {
            if (TRACE) {
                try {
                    Path outZip = Path.of("build/trace/naver_trace_" + ts + ".zip");
                    Files.createDirectories(outZip.getParent());
                    context.tracing().stop(new Tracing.StopOptions().setPath(outZip));
                    log.debug("[NP] trace saved {}", outZip.toAbsolutePath());
                } catch (Throwable ignored) {}
            }
            try { page.close(); } catch (Throwable ignored) {}
        }
    }

    /** === 유틸 === */

    private static void waitGenerously(Page p) {
        try { p.waitForLoadState(LoadState.DOMCONTENTLOADED); } catch (Throwable ignored) {}
        try { p.waitForLoadState(LoadState.LOAD); } catch (Throwable ignored) {}
        try { p.waitForLoadState(LoadState.NETWORKIDLE); } catch (Throwable ignored) {}
        try {
            p.waitForFunction(
                    "() => document.readyState==='complete' || " +
                            "document.querySelector('iframe#searchIframe') || " +
                            "document.querySelector('#_pcmap_list_scroll_container')",
                    new Page.WaitForFunctionOptions().setTimeout(8_000)
            );
        } catch (Throwable ignored) {}
        p.waitForTimeout(ThreadLocalRandom.current().nextInt(1_200, 2_600));
    }

    private static void ensureDesktop(Page p) {
        try {
            p.waitForFunction(
                    "() => window.innerWidth >= 1000 && !location.host.startsWith('m.')",
                    new Page.WaitForFunctionOptions().setTimeout(3_000)
            );
        } catch (Throwable ignored) {}
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
            p.waitForTimeout(150);
        }
        return null;
    }

    private static void lazyScrollInFrame(Frame f, String containerSel, int steps) {
        for (int i = 0; i < steps; i++) {
            try {
                f.evaluate("sel => { " +
                        "const c = document.querySelector(sel) || document.querySelector('div#ct') || document.body; " +
                        "if (c) c.scrollBy(0, Math.floor(700 + Math.random()*500)); }", containerSel);
            } catch (Throwable ignored) {}
            sleep(rand(90, 160));
        }
    }

    private static String extractPlaceId(String url) {
        if (url == null) return null;
        for (Pattern pt : new Pattern[]{ PID_RESTAURANT, PID_ENTRY, PID_PLACE, PID_QUERY }) {
            Matcher m = pt.matcher(url);
            if (m.find()) return m.group(1);
        }
        return null;
    }


    private static boolean tryJsClick(Locator loc) {
        try { loc.evaluate("el => el.click()"); return true; }
        catch (Throwable e) { return false; }
    }

    private static void clickIfVisible(Page p, String selector, String tag) {
        try {
            Locator x = p.locator(selector).first();
            if (x.count() > 0 && x.isVisible()) {
                x.click(new Locator.ClickOptions().setTimeout(1_500));
                log.debug("{} clicked selector={}", tag, selector);
            }
        } catch (Throwable ignored) {}
    }

    private static String textOrEmpty(Locator loc) {
        try { return loc.innerText(); } catch (Throwable e) { return ""; }
    }

    private static String safe(String s) { return s == null ? "" : s.trim(); }
    private static void jitter(Page p, int minMs, int maxMs) { p.waitForTimeout(rand(minMs, maxMs)); }
    private static int rand(int lo, int hi) { return ThreadLocalRandom.current().nextInt(lo, hi + 1); }
    private static void sleep(long ms) { try { Thread.sleep(ms); } catch (InterruptedException ignored) {} }

    private static void dumpFrames(Page p, String tag) {
        if (!DEBUG) return;
        try {
            StringBuilder sb = new StringBuilder(tag).append(" frames=");
            for (Frame f : p.frames()) {
                sb.append("[name=").append(f.name())
                        .append(", url=").append(trimUrl(f.url())).append("] ");
            }
            log.debug(sb.toString());
        } catch (Throwable ignored) {}
    }

    private static String trimUrl(String u) {
        if (u == null) return null;
        if (u.length() <= 140) return u;
        int q = u.indexOf('?');
        return q > 0 ? u.substring(0, Math.min(q, 140)) + "...": u.substring(0, 140) + "...";
    }

    private static String safePreview(String body, int max) {
        try { return body == null ? null : body.substring(0, Math.min(max, body.length())); }
        catch (Throwable e) { return null; }
    }

    private static void safeShot(Page p, String name) {
        if (!SHOT) return;
        try {
            Path dir = Path.of("build", "screenshots");
            Files.createDirectories(dir);
            Path path = dir.resolve("naver_" + name + ".png");
            p.screenshot(new Page.ScreenshotOptions().setPath(path).setFullPage(true));
            log.debug("[NP] screenshot {}", path.toAbsolutePath());
        } catch (Throwable ignored) {}
    }
}
