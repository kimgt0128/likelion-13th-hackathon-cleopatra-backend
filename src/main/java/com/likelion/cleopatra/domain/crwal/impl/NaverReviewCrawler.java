package com.likelion.cleopatra.domain.crwal.impl;

import com.likelion.cleopatra.domain.crwal.dto.place.NaverPlaceReview;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class NaverReviewCrawler {

    /** Playwright Page는 외부(서비스/빈)에서 주입 */
    private final Page page;

    /** placeLink에서 최대 count개 리뷰 수집 */
    public List<NaverPlaceReview> crawlReviews(String placeLink, int count) {
        List<NaverPlaceReview> out = new ArrayList<>();
        log.info("[NaverReviewCrawler] 시작 url={} count={}", placeLink, count);

        try {
            page.setDefaultTimeout(12_000);
            page.navigate(placeLink);
            page.waitForLoadState(LoadState.NETWORKIDLE);
            log.debug("[NaverReviewCrawler] 페이지 로딩 완료");

            // entry iframe 진입
            Locator iframeLoc = page.locator("iframe#entryIframe");
            iframeLoc.waitFor();
            log.debug("[NaverReviewCrawler] entryIframe 탐지");
            FrameLocator f = page.frameLocator("#entryIframe");

            // "리뷰" 탭 클릭 (텍스트 기반, 조상 a에 클릭)
            f.locator("span:has-text(\"리뷰\")").first().click();
            log.info("[NaverReviewCrawler] 리뷰 탭 클릭");

            // 리뷰 리스트 로딩 대기
            Locator ul = f.locator("ul#_review_list");
            ul.waitFor();
            log.debug("[NaverReviewCrawler] 리뷰 리스트 감지");

            // 스크롤로 count개 이상 로드 시도
            Locator lis = ul.locator(":scope > li.place_apply_pui");
            int attempts = 0, last = -1;
            while (lis.count() < count && attempts++ < 20) {
                ul.evaluate("el => el.scrollTop = el.scrollHeight");
                page.waitForTimeout(350);
                int now = lis.count();
                log.debug("[NaverReviewCrawler] 스크롤 로드 시도={} 현재항목수={}", attempts, now);
                if (now == last) break;
                last = now;
            }
            int total = lis.count();
            int limit = Math.min(count, total);
            log.info("[NaverReviewCrawler] 파싱 시작 total={} limit={}", total, limit);

            for (int i = 0; i < limit; i++) {
                Locator li = lis.nth(i);

                // 방문 키워드
                List<String> visitKeywords = cleanTexts(
                        li.locator("a[data-pui-click-code='visitkeywords'] span").allInnerTexts()
                );

                // 본문: 더보기 있으면 펼친 뒤 읽기 (안전하게 컨테이너 기준)
                Locator bodyBox = li.locator("div.pui__vn15t2");
                if (li.locator("a.pui__wFzIYl").isVisible()) {
                    li.locator("a.pui__wFzIYl").click(); // 더보기
                    page.waitForTimeout(120);
                    log.debug("[NaverReviewCrawler] #{} 본문 더보기 클릭", i + 1);
                }
                String body = normalize(safeInnerText(bodyBox));
                // 더보기 텍스트 흔적 제거
                body = body.replace("더보기", "").trim();

                // 재방문 문구 (예: "2번째 방문")
                String revisit = "";
                List<String> spans = li.locator("span").allInnerTexts();
                for (String s : spans) {
                    if (s != null && s.contains("번째 방문")) {
                        revisit = s.trim();
                        break;
                    }
                }

                // 만족 태그 칩 (a 제외)
                List<String> tags = cleanTexts(
                        li.locator("div.pui__HLNvmI :not(a).pui__jhpEyP").allInnerTexts()
                );

                NaverPlaceReview review = NaverPlaceReview.builder()
                        .link(placeLink)
                        .visitKeywords(visitKeywords)
                        .body(body)
                        .revisit(revisit)
                        .tags(tags)
                        .build();

                out.add(review);
                log.info("[NaverReviewCrawler] 수집 #{}/{} visitKeywords={} revisit='{}' tags={} bodyLen={}",
                        i + 1, limit, visitKeywords.size(), revisit, tags.size(), body.length());
            }

            log.info("[NaverReviewCrawler] 완료 수집건수={}", out.size());
            return out;

        } catch (PlaywrightException e) {
            log.error("[NaverReviewCrawler] 실패: {}", e.getMessage(), e);
            return out;
        }
    }

    /** 기존 시그니처 유지: count번째(1-based) 리뷰 1건 */
    public NaverPlaceReview crawlReview(String placeLink, int count) {
        List<NaverPlaceReview> list = crawlReviews(placeLink, count);
        if (list.isEmpty()) return null;
        int idx = Math.min(count, list.size()) - 1;
        NaverPlaceReview rv = list.get(idx);
        log.info("[NaverReviewCrawler] 단건 반환 index={} revisit='{}' bodyLen={}", idx, rv.getRevisit(), rv.getBody() == null ? 0 : rv.getBody().length());
        return rv;
    }

    // ---------- helpers ----------
    private static String safeInnerText(Locator locator) {
        try {
            return locator.innerText();
        } catch (Throwable t) {
            return "";
        }
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
        return list.stream()
                .map(NaverReviewCrawler::normalize)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toList());
    }
}
