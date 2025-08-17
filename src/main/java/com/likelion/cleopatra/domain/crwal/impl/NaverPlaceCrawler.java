// impl/NaverPlaceCrawler.java
package com.likelion.cleopatra.domain.crwal.impl;

import com.likelion.cleopatra.domain.crwal.dto.place.NaverPlaceContentRes;
import com.likelion.cleopatra.domain.crwal.dto.place.NaverPlaceReview;
import com.likelion.cleopatra.domain.crwal.selector.NaverPlaceSelectors;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class NaverPlaceCrawler {

    private final BrowserContext context;

    private static final Pattern PID_IN_URL_1 = Pattern.compile("/restaurant/(\\d+)");
    private static final Pattern PID_IN_URL_2 = Pattern.compile("[?&]placeId=(\\d+)");

    /** 키워드로 지도 검색 → 상위 placeLimit 매장 각각의 방문자 리뷰(perReview개) 수집 */
    public List<NaverPlaceContentRes> crawl(String keyword, int placeLimit, int perReview) {
        final String searchUrl = "https://map.naver.com/p/search/" + URLEncoder.encode(keyword, StandardCharsets.UTF_8);

        Page page = context.newPage();
        page.setDefaultTimeout(12_000);
        page.setDefaultNavigationTimeout(15_000);
        page.route("**/*", r -> {
            String t = r.request().resourceType();
            if ("media".equals(t) || "image".equals(t) || "font".equals(t)) r.abort(); else r.resume();
        });

        List<NaverPlaceContentRes> results = new ArrayList<>();

        try {
            page.navigate(searchUrl);
            page.waitForLoadState(LoadState.NETWORKIDLE);

            Frame search = waitFrame(page, "iframe#searchIframe", 8_000);
            if (search == null) throw new RuntimeException("searchIframe not found");

            lazyScroll(search, "#_pcmap_list_scroll_container", 6);

            Locator cards = search.locator("a[href*='place.naver.com']");
            int targetCount = Math.min(cards.count(), Math.max(1, placeLimit));

            for (int i = 0; i < targetCount; i++) {
                try {
                    cards.nth(i).click();
                    Frame entry = waitFrame(page, "iframe#entryIframe", 8_000);
                    if (entry == null) throw new RuntimeException("entryIframe not found");

                    String entryUrl = entry.url();
                    String placeId = extractPlaceId(entryUrl);
                    if (placeId == null) throw new RuntimeException("placeId not found: " + entryUrl);

                    NaverPlaceContentRes one = crawlReviewsOnMobile(placeId, perReview);
                    results.add(one);

                    page.waitForTimeout(200);
                } catch (Exception ignore) {
                    // 실패한 매장은 스킵
                }
            }

            return results;

        } finally {
            page.close();
        }
    }

    /** 단일 placeId의 모바일 방문자 리뷰 페이지를 열어 perReview개 수집 후 구조로 반환 */
    private NaverPlaceContentRes crawlReviewsOnMobile(String placeId, int perReview) {
        final String url = "https://m.place.naver.com/restaurant/" + placeId + "/review/visitor";
        Page p = context.newPage();
        p.setDefaultTimeout(10_000);
        p.setDefaultNavigationTimeout(15_000);
        p.route("**/*", r -> {
            String t = r.request().resourceType();
            if ("media".equals(t) || "image".equals(t) || "font".equals(t)) r.abort(); else r.resume();
        });

        try {
            p.navigate(url);
            p.waitForLoadState(LoadState.NETWORKIDLE);
            autoScroll(p, 3_000);

            List<NaverPlaceReview> reviews = new ArrayList<>();
            int seen = 0;

            while (reviews.size() < perReview) {
                Locator cards = p.locator(NaverPlaceSelectors.REVIEW_CARD);
                int total = cards.count();
                if (total <= seen) break;

                for (int i = seen; i < total && reviews.size() < perReview; i++) {
                    Locator card = cards.nth(i);
                    expandIfAny(card);

                    @SuppressWarnings("unchecked")
                    Map<String, Object> m = (Map<String, Object>) card.evaluate("c=>{"
                            + "const visit=[...c.querySelectorAll('" + NaverPlaceSelectors.VISIT_KEYWORDS + "')]"
                            + " .map(e=>(e.textContent||'').trim()).filter(Boolean);"
                            + "const wrap=c.querySelector('" + NaverPlaceSelectors.REVIEW_TEXT + "');"
                            + "const body=wrap? (wrap.innerText||'').replace('접기','').trim() : '';"
                            + "let revisit=null; for(const s of c.querySelectorAll('span')){"
                            + " const t=(s.textContent||'').trim(); if(t.includes('" + NaverPlaceSelectors.REVISIT_CONTAINS + "')){revisit=t;break;}}"
                            + "let tags=[]; for(const p of c.querySelectorAll('" + NaverPlaceSelectors.TAG_CHIPS_PARENT + "')){"
                            + " if(p.querySelector('img')){ const t=(p.textContent||'').trim(); if(t) tags.push(t);} }"
                            + "return {visit, body, revisit, tags}; }");

                    List<String> visit = castList(m.get("visit"));
                    String body = safe((String) m.get("body"));
                    String revisit = safe((String) m.get("revisit"));
                    List<String> tags = dedup(castList(m.get("tags")));

                    if (visit.isEmpty() && body.isBlank() && revisit.isBlank() && tags.isEmpty()) continue;

                    reviews.add(NaverPlaceReview.builder()
                            .visitKeywords(visit)
                            .body(body)
                            .revisit(revisit)
                            .tags(tags)
                            .build());
                }

                seen = total;
                if (reviews.size() >= perReview) break;

                autoScroll(p, 2_500);
            }

            String placeName = p.title().replace(" - 네이버 플레이스","").trim();

            return NaverPlaceContentRes.builder()
                    .placeId(placeId)
                    .placeName(placeName)
                    .placeUrl(url)
                    .reviews(reviews)
                    .build();

        } finally {
            p.close();
        }
    }

    // --- 유틸 ---

    private static Frame waitFrame(Page p, String frameCss, int timeoutMs) {
        ElementHandle h = p.waitForSelector(frameCss,
                new Page.WaitForSelectorOptions().setTimeout(timeoutMs));
        return h == null ? null : h.contentFrame();
    }

    private static void lazyScroll(Frame f, String scrollSel, int steps) {
        for (int i = 0; i < steps; i++) {
            try { f.evaluate("sel => { const c=document.querySelector(sel); if(c) c.scrollBy(0, 900); }", scrollSel); }
            catch (Exception ignore) {}
            try { Thread.sleep(160); } catch (InterruptedException ignored) {}
        }
    }

    private static void autoScroll(Page p, int limitPx) {
        p.evaluate("L=>new Promise(r=>{let y=0,st=700;let id=setInterval(()=>{window.scrollBy(0,st);y+=st;if(y>=L){clearInterval(id);r(0)}},220)})", limitPx);
        p.waitForTimeout(350);
    }

    private static String extractPlaceId(String url) {
        Matcher m1 = PID_IN_URL_1.matcher(url);
        if (m1.find()) return m1.group(1);
        Matcher m2 = PID_IN_URL_2.matcher(url);
        if (m2.find()) return m2.group(1);
        return null;
    }

    private static String safe(String s) { return s == null ? "" : s.trim(); }

    @SuppressWarnings("unchecked")
    private static List<String> castList(Object o) {
        if (!(o instanceof List<?> l)) return Collections.emptyList();
        List<String> r = new ArrayList<>(l.size());
        for (Object x : l) {
            String s = (x == null) ? "" : x.toString().trim();
            if (!s.isBlank()) r.add(s);
        }
        return r;
    }

    private static List<String> dedup(List<String> in) {
        return new ArrayList<>(new LinkedHashSet<>(in));
    }

    private void expandIfAny(Locator card){
        try{
            Locator more = card.locator(NaverPlaceSelectors.REVIEW_TEXT_MORE);
            if (more.count() > 0 && more.first().isVisible()) {
                more.first().click(new Locator.ClickOptions().setTimeout(1500));
            }
        } catch (PlaywrightException ignore) { }
    }
}
