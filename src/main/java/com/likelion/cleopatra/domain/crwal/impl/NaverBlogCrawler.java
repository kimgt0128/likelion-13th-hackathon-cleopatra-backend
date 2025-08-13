package com.likelion.cleopatra.domain.crwal.impl;

import com.likelion.cleopatra.domain.crwal.dto.NaverBlogCrawlRes;
import com.likelion.cleopatra.domain.crwal.selector.NaverSelectors;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitUntilState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 네이버는 PC 페이지가 iframe으로 본문을 넣는 경우가 있어 모바일 URL로 변환해 파싱한다.
 * 도커에서 실행 시 Playwright 런타임 라이브러리(폰트, libnss 등)가 필요할 수 있다. 초기엔 로컬에서 검증 후 컨테이너 의존성 추가.
 * 크롤링 속도 조절이 필요하면 page.waitForTimeout()을 소폭 삽입하거나 배치 크기를 줄인다.
 */
@RequiredArgsConstructor
@Component
public class NaverBlogCrawler {

    private final BrowserContext context;


    public NaverBlogCrawlRes crawl(String originalUrl) {
        String url = toMobileUrl(originalUrl);

        Page page = context.newPage(); // crawl exception 생성 후 예외 처리 로직 추가

        page.navigate(url, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

        // 간헐적 팝업 제거
        page.locator("button:has-text('닫기'), .btn_close, .u_skip").first().click(new Locator.ClickOptions().setTrial(true).setTimeout(1000));//.catchException(_e -> {});

        // 본문 대기
        Locator se = page.locator(NaverSelectors.SE_MAIN);
        Locator legacy = page.locator(NaverSelectors.LEGACY_BODY);

        // 스크롤 유도 후 재시도
        if (se.count() == 0 && legacy.count() == 0) {
            // 스크롤 유도 후 재시도
            page.mouse().wheel(0, 2000);
            page.waitForTimeout(800);
        }

        String title = extractTitle(page);
        String html = extractHtml(page);
        String text = extractTextFromHtml(page, html);
        if (html == null || html.isBlank()) {
            throw new IllegalStateException("본문 선택자 미일치");
        }
        return new NaverBlogCrawlRes(title, html, text);
    }




    private String toMobileUrl(String url) {
        return url.replace("://blog.naver.com/", "://m.blog.naver.com/");
    }

    private String extractTitle(Page page) {
        // 여러 후보 중 우선 매칭
        for (String sel : NaverSelectors.TITLE.split(",")) {
            Locator l = page.locator(sel.trim());
            if (l.count() > 0) {
                String t = l.first().innerText().trim();
                if (!t.isBlank()) return t;
            }
        }
        return page.title();
    }

    private String extractHtml(Page page) {
        Locator se = page.locator(NaverSelectors.SE_MAIN);
        if (se.count() > 0) return se.first().innerHTML();

        Locator legacy = page.locator(NaverSelectors.LEGACY_BODY);
        if (legacy.count() > 0) return legacy.first().innerHTML();

        // 최후 수단: og:description만
        Locator og = page.locator("meta[property='og:description']");
        if (og.count() > 0) return "<p>" + og.first().getAttribute("content") + "</p>";
        return null;
    }

    private String extractTextFromHtml(Page page, String html) {
        // 페이지에서 DOM API로 텍스트만 추출
        // se-text 블록 우선
        Locator blocks = page.locator(NaverSelectors.SE_TEXT_BLOCKS);
        if (blocks.count() > 0) {
            List<String> lines = blocks.allInnerTexts();
            return String.join("\n\n", lines).trim();
        }
        // 그 외는 전체 컨테이너의 innerText
        Locator se = page.locator(NaverSelectors.SE_MAIN);
        if (se.count() > 0) return se.first().innerText().trim();

        Locator legacy = page.locator(NaverSelectors.LEGACY_BODY);
        if (legacy.count() > 0) return legacy.first().innerText().trim();

        return "";
    }

}
