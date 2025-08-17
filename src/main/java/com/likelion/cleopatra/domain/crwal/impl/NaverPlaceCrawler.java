// src/main/java/com/likelion/cleopatra/domain/crwal/impl/NaverPlaceCrawler.java
package com.likelion.cleopatra.domain.crwal.impl;

import com.likelion.cleopatra.domain.crwal.dto.place.NaverPlaceLinkRes;
import com.likelion.cleopatra.domain.crwal.selector.NaverPlaceSelectors;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.microsoft.playwright.options.WaitUntilState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

    public List<NaverPlaceLinkRes> crawlLinks(String keyword, int placeLimit) {
        String enc = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
        String pSearch = "https://map.naver.com/p/search/" + enc + "?c=14.00,0,0,0,dh";
        String v5Search = "https://map.naver.com/v5/search/" + enc;
        String pcmapList = "https://pcmap.place.naver.com/place/list?query=" + enc;

        Page page = context.newPage();
        // 리소스 차단 최소화: media만 차단
        page.route("**/*", r -> {
            String t = r.request().resourceType();
            if ("media".equals(t)) r.abort();
            else r.resume();
        });

        page.setDefaultTimeout(25_000);
        page.setDefaultNavigationTimeout(30_000);
        List<NaverPlaceLinkRes> out = new ArrayList<>();

        try {
            page.navigate(pSearch, new Page.NavigateOptions().setTimeout(30_000).setWaitUntil(WaitUntilState.LOAD));
            waitGenerously(page);
            clickIfVisible(page.locator(NaverPlaceSelectors.BANNER_ACCEPT));

            // 데스크톱 보장 확인
            ensureDesktop(page);

            Frame searchFrame = waitIframeByIdOrUrl(page, "searchIframe", "/search", 9_000);
            if (searchFrame == null) {
                log.debug("[NP] searchIframe miss → v5 전환");
                page.navigate(v5Search, new Page.NavigateOptions().setTimeout(30_000).setWaitUntil(WaitUntilState.LOAD));
                waitGenerously(page);
                clickIfVisible(page.locator(NaverPlaceSelectors.BANNER_ACCEPT));
                ensureDesktop(page);
                searchFrame = waitIframeByIdOrUrl(page, "searchIframe", "/search", 9_000);
            }
            if (searchFrame == null) {
                log.debug("[NP] searchIframe miss 2 → pcmap list 전환");
                page.navigate(pcmapList, new Page.NavigateOptions().setTimeout(30_000).setWaitUntil(WaitUntilState.LOAD));
                waitGenerously(page);
                clickIfVisible(page.locator(NaverPlaceSelectors.BANNER_ACCEPT));
                ensureDesktop(page);
                searchFrame = waitIframeByIdOrUrl(page, "searchIframe", "/place/list", 9_000);
            }
            // 프레임이 끝내 없다면 모바일/단일 DOM. 폴백 경로로 진행.
            boolean frameLess = (searchFrame == null);

            int limit = Math.max(1, placeLimit);
            Locator items;
            if (!frameLess) {
                lazyScrollInFrame(searchFrame, NaverPlaceSelectors.SEARCH_LIST_CONTAINER, rand(5, 8));
                items = searchFrame.locator(NaverPlaceSelectors.SEARCH_LIST_ITEMS);
                if (items.count() == 0) {
                    items = searchFrame.locator("xpath=/html/body/div[3]/div/div[2]/div[1]/ul/li");
                }
            } else {
                // 모바일/단일 DOM 폴백
                items = page.locator(
                        "#_pcmap_list_scroll_container ul > li, " +               // 데스크톱
                                "ul._3jtfG > li, ul.RgCez > li, div._1xg8B > ul > li"     // 모바일 추정 폴백
                );
                if (items.count() == 0) {
                    // 마지막 폴백: 화면 스크롤 유도 후 재탐색
                    for (int k = 0; k < 6; k++) {
                        page.evaluate("() => window.scrollBy(0, 900)");
                        page.waitForTimeout(150);
                    }
                    items = page.locator("#_pcmap_list_scroll_container ul > li");
                }
            }

            int available = items.count();
            int target = Math.min(available, limit);
            log.debug("[NP] list count={} target={}", available, target);

            for (int i = 0; i < target; i++) {
                try {
                    Locator li = items.nth(i);
                    Locator link = li.locator(NaverPlaceSelectors.SEARCH_CARD_LINK).first();
                    String name = safe(textOrEmpty(li.locator("span.TYaxT, span.YwYLL").first()));
                    if (name.isBlank()) name = safe(textOrEmpty(link));

                    if (!tryJsClick(link)) {
                        link.click(new Locator.ClickOptions().setForce(true).setTimeout(12_000));
                    }
                    page.waitForTimeout(800);

                    // 1) 최상위 URL에서 placeId 폴백 회수
                    String pid = extractPlaceId(page.url());

                    // 2) entryIframe가 있으면 재확인
                    Frame entry = waitIframeByIdOrUrl(page, "entryIframe", "/entry", 6_000);
                    if (entry != null) {
                        String eurl = entry.url();
                        String pid2 = extractPlaceId(eurl);
                        if (pid == null) pid = pid2;
                    }

                    if (pid == null) {
                        log.debug("[NP] placeId 추출 실패. idx={}", i);
                        continue;
                    }

                    String canonical = "https://map.naver.com/p/search/" + enc + "/place/" + pid;
                    out.add(NaverPlaceLinkRes.builder()
                            .placeId(pid)
                            .placeName(name)
                            .placeUrl(canonical)
                            .build());

                    jitter(page, 120, 240);
                } catch (Exception e) {
                    log.debug("[NP] skip idx={} reason={}", i, e.toString());
                }
            }
            log.debug("[NP] done count={}", out.size());
            return out;

        } finally {
            page.close();
        }
    }

    /** 관대한 로딩 대기 */
    private static void waitGenerously(Page p) {
        try { p.waitForLoadState(LoadState.DOMCONTENTLOADED); } catch (PlaywrightException ignored) {}
        try { p.waitForLoadState(LoadState.LOAD); } catch (PlaywrightException ignored) {}
        try { p.waitForLoadState(LoadState.NETWORKIDLE); } catch (PlaywrightException ignored) {}
        try {
            p.waitForFunction(
                    "() => document.readyState==='complete' || " +
                            "document.querySelector('iframe#searchIframe') || " +
                            "document.querySelector('#_pcmap_list_scroll_container')",
                    new Page.WaitForFunctionOptions().setTimeout(8_000));
        } catch (PlaywrightException ignored) {}
        p.waitForTimeout(ThreadLocalRandom.current().nextInt(1_200, 2_600));
    }

    /** 모바일 전환 방지용 점검 */
    private static void ensureDesktop(Page p) {
        try {
            p.waitForFunction(
                    "() => window.innerWidth >= 1000 && !location.host.startsWith('m.')",
                    new Page.WaitForFunctionOptions().setTimeout(3_000));
        } catch (PlaywrightException ignored) {}
    }

    /** 프레임 탐지: id 또는 URL 부분일치 */
    private static Frame waitIframeByIdOrUrl(Page p, String id, String urlSub, int timeoutMs) {
        long end = System.currentTimeMillis() + Math.max(1000, timeoutMs);
        while (System.currentTimeMillis() < end) {
            // id 기반 우선
            try {
                ElementHandle h = p.querySelector("iframe#" + id);
                if (h != null) {
                    Frame f = h.contentFrame();
                    if (f != null) return f;
                }
            } catch (Exception ignored) {}

            // URL 기반
            for (Frame f : p.frames()) {
                try {
                    String u = f.url();
                    String n = f.name();
                    if ((n != null && n.equals(id)) || (u != null && u.contains(urlSub))) {
                        return f;
                    }
                } catch (Exception ignored) {}
            }
            p.waitForTimeout(150);
        }
        return null;
    }

    /** 프레임 내부 스크롤 */
    private static void lazyScrollInFrame(Frame f, String containerSel, int steps) {
        for (int i = 0; i < steps; i++) {
            try {
                f.evaluate("sel => { " +
                        "const c = document.querySelector(sel) || document.querySelector('div#ct') || document.body; " +
                        "if (c) c.scrollBy(0, Math.floor(700 + Math.random()*500)); }", containerSel);
            } catch (Exception ignored) {}
            sleep(rand(90, 160));
        }
    }

    /** placeId 추출 */
    private static String extractPlaceId(String url) {
        if (url == null) return null;
        for (Pattern pt : new Pattern[]{PID_ENTRY, PID_RESTAURANT, PID_QUERY}) {
            Matcher m = pt.matcher(url);
            if (m.find()) return m.group(1);
        }
        return null;
    }

    private static boolean tryJsClick(Locator loc) {
        try { loc.evaluate("el => el.click()"); return true; }
        catch (PlaywrightException e) { return false; }
    }

    private static void clickIfVisible(Locator loc) {
        try {
            Locator x = loc.first();
            if (x.count() > 0 && x.isVisible())
                x.click(new Locator.ClickOptions().setTimeout(1_500));
        } catch (Exception ignored) {}
    }

    private static String textOrEmpty(Locator loc) {
        try { return loc.innerText(); } catch (Exception e) { return ""; }
    }

    private static String safe(String s) { return s == null ? "" : s.trim(); }
    private static void jitter(Page p, int minMs, int maxMs) { p.waitForTimeout(rand(minMs, maxMs)); }
    private static int rand(int lo, int hi) { return ThreadLocalRandom.current().nextInt(lo, hi + 1); }
    private static void sleep(long ms) { try { Thread.sleep(ms); } catch (InterruptedException ignored) {} }
}
