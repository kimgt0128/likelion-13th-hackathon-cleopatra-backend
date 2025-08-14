package com.likelion.cleopatra.domain.crwal.impl;

import com.likelion.cleopatra.domain.crwal.dto.NaverBlogContentRes;
import com.likelion.cleopatra.domain.crwal.selector.NaverSelectors;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitUntilState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;

@RequiredArgsConstructor
@Component
@Slf4j
public class NaverBlogCrawler {

    private final BrowserContext context;

    public NaverBlogContentRes crawl(String originalUrl) {
        final String url = toMobileUrl(originalUrl);
        final Page page = context.newPage();
        try {
            // 리소스 최소 차단: media만
            page.route("**/*", r -> {
                String t = r.request().resourceType();
                if ("media".equals(t)) r.abort(); else r.resume();
            });

            // 랜덤 뷰포트 + 짧은 지터
            page.setViewportSize(360 + rnd(0, 80), 740 + rnd(0, 180));
            page.navigate(url, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
            page.waitForTimeout(rnd(250, 900));

            // 선택적 확장 클릭(실패 시 예외 전파)
            clickIfPresent(page, "button:has-text('닫기'), .btn_close, .u_skip", 1200);
            clickIfPresent(page, "button:has-text('더보기')", 1200);
            clickIfPresent(page, ".se-more-button, .se-more-text", 1200);
            clickIfPresent(page, "a:has-text('원문보기')", 1500);

            // 본문 컨테이너 필수 확보(orElseThrow 패턴)
            Locator body = requireBody(page);

            String title = extractTitle(page)
                    .trim();
            String html = body.innerHTML();
            String text = body.innerText().trim();

            // 너무 짧으면 경량 스크롤 1회 후 재추출
            if (text.length() < 50) {
                autoScrollLight(page);
                html = body.innerHTML();
                text = body.innerText().trim();
            }

            if (html.isBlank())
                throw new IllegalStateException("본문 선택자 미일치");

            // 배포 최소 로그
            log.info("NAVER_BLOG crawled url={} textLen={}", url, text.length());
            // 개발 상세 로그
            log.debug("NAVER_BLOG result url={} title='{}' htmlLen={} textLen={} sample=\"{}\"",
                    url, compact(title), html.length(), text.length(), sample(text, 160));

            return new NaverBlogContentRes(title, html, text);
        } finally {
            page.close(); // 자원 정리, 예외는 위로 전파
        }
    }

    // --- helpers ---

    private Locator requireBody(Page page) {
        Locator se = page.locator(NaverSelectors.SE_MAIN);
        if (se.count() > 0) return se.first();
        Locator legacy = page.locator(NaverSelectors.LEGACY_BODY);
        if (legacy.count() > 0) return legacy.first();
        // 마지막 짧은 대기 후 재확인
        page.waitForTimeout(300);
        if (se.count() > 0) return se.first();
        if (legacy.count() > 0) return legacy.first();
        throw new IllegalStateException("본문 컨테이너 없음");
    }

    private void clickIfPresent(Page p, String sel, int timeoutMs) {
        Locator l = p.locator(sel).first();
        if (l.count() > 0) l.click(new Locator.ClickOptions().setTimeout(timeoutMs));
    }

    private void autoScrollLight(Page page) {
        page.evaluate("() => new Promise(res => {"
                + "let y=0, step=500, max=Math.min(2500, document.body.scrollHeight);"
                + "let id=setInterval(()=>{window.scrollBy(0,step); y+=step; if(y>=max){clearInterval(id);res(0)}},250);})");
        page.waitForTimeout(rnd(200, 500));
    }

    private String extractTitle(Page page) {
        for (String sel : NaverSelectors.TITLE.split(",")) {
            Locator l = page.locator(sel.trim());
            if (l.count() > 0) {
                if (sel.contains("meta")) {
                    String v = l.first().getAttribute("content");
                    if (v != null && !v.isBlank()) return v;
                } else {
                    String t = l.first().innerText();
                    if (t != null && !t.isBlank()) return t;
                }
            }
        }
        return page.title();
    }

    private static int rnd(int a, int b) {
        return ThreadLocalRandom.current().nextInt(a, b + 1);
    }
    private static String sample(String s, int n) {
        if (s == null) return "";
        String one = s.replaceAll("\\s+", " ").trim();
        return one.substring(0, Math.min(one.length(), n));
    }
    private static String compact(String s) {
        return s == null ? "" : s.replaceAll("\\s+", " ").trim();
    }
    private String toMobileUrl(String url) {
        return url.replace("://blog.naver.com/", "://m.blog.naver.com/");
    }
}
