package com.likelion.cleopatra.domain.crwal.impl;

import com.likelion.cleopatra.domain.crwal.dto.NaverBlogContentRes;
import com.likelion.cleopatra.domain.crwal.selector.NaverSelectors;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
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

            // [ADD] 기본 타임아웃 현실화 (팝업/렌더 지연에 대비)
            page.setDefaultTimeout(10_000);
            page.setDefaultNavigationTimeout(15_000);

            // 리소스 최소 차단: media만
            page.route("**/*", r -> {
                String t = r.request().resourceType();
                if ("media".equals(t)) r.abort(); else r.resume();
            });

            // 랜덤 뷰포트 + 짧은 지터
            page.setViewportSize(360 + rnd(0, 80), 740 + rnd(0, 180));
            page.navigate(url, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
            page.waitForLoadState(LoadState.NETWORKIDLE); // [ADD] 네트워크 안정화까지 대기
            page.waitForTimeout(rnd(250, 900));

            // [CHG] 선택적 클릭을 “best-effort”로. 실패해도 전파하지 않음
            clickIfPresent(page, "button:has-text('닫기'), .btn_close, .u_skip", 1200);
            clickIfPresent(page, "button:has-text('더보기')", 1200);
            clickIfPresent(page, ".se-more-button, .se-more-text", 1200);
            clickIfPresent(page, "a:has-text('원문보기')", 1500);

            // [CHG] 본문 컨테이너 필수 확보: 모바일 우선 → 레거시 → iframe(mainFrame) 순
            Locator body = requireBody(page);

            String title = extractTitle(page).trim();
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
        // 1) 모바일 신형
        Locator se = page.locator(NaverSelectors.SE_MAIN);
        if (se.count() > 0) return se.first();

        // 2) 모바일/데스크톱 레거시
        Locator legacy = page.locator(NaverSelectors.LEGACY_BODY);
        if (legacy.count() > 0) return legacy.first();

        // 3) [ADD] 데스크톱 구형 iframe(mainFrame) 대응
        Frame main = null;
        for (Frame f : page.frames()) {
            if ("mainFrame".equals(f.name())) { main = f; break; }
        }
        if (main != null) {
            Locator fSe = main.locator(NaverSelectors.SE_MAIN);
            if (fSe.count() > 0) return fSe.first();
            Locator fLegacy = main.locator(NaverSelectors.LEGACY_BODY);
            if (fLegacy.count() > 0) return fLegacy.first();
        }

        // 마지막 짧은 대기 후 재확인
        page.waitForTimeout(300);
        if (se.count() > 0) return se.first();
        if (legacy.count() > 0) return legacy.first();
        if (main != null) {
            Locator fSe = main.locator(NaverSelectors.SE_MAIN);
            if (fSe.count() > 0) return fSe.first();
            Locator fLegacy = main.locator(NaverSelectors.LEGACY_BODY);
            if (fLegacy.count() > 0) return fLegacy.first();
        }

        throw new IllegalStateException("본문 컨테이너 없음");
    }

    // [CHG] 클릭 실패는 무시(TimeoutError/PlaywrightException), 보이는 경우만 클릭
    private void clickIfPresent(Page p, String sel, int timeoutMs) {
        Locator list = p.locator(sel);
        if (list.count() == 0) return;
        Locator first = list.first();
        try {
            if (first.isVisible()) {
                first.click(new Locator.ClickOptions().setTimeout(timeoutMs));
                p.waitForTimeout(150); // [ADD] 클릭 후 안정화
            } else {
                log.debug("click skip sel={} reason=not-visible", sel); // [ADD]
            }
        } catch (PlaywrightException e) {
            log.debug("click ignore sel={} msg={}", sel, compact(e.getMessage())); // [ADD]
        }
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
