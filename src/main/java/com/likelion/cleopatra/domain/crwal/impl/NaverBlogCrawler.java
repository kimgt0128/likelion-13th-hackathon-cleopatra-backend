package com.likelion.cleopatra.domain.crwal.impl;

import com.likelion.cleopatra.domain.crwal.dto.NaverBlogContentRes;
import com.likelion.cleopatra.domain.crwal.selector.NaverSelectors;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitUntilState;
import com.microsoft.playwright.options.WaitForSelectorState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function; // [ADD]
import java.util.List;             // [ADD]

@RequiredArgsConstructor
@Component
@Slf4j
public class NaverBlogCrawler {

    private final BrowserContext context;

    public NaverBlogContentRes crawl(String originalUrl) {
        final String url = toMobileUrl(originalUrl);
        final Page page = context.newPage();
        try {
            // [ADD] 기본 타임아웃 현실화
            page.setDefaultTimeout(10_000);
            page.setDefaultNavigationTimeout(15_000);

            // [CHG] 텍스트만 필요 → image/media/font 차단
            page.route("**/*", r -> {
                String t = r.request().resourceType();
                if ("media".equals(t) || "image".equals(t) || "font".equals(t)) r.abort(); else r.resume();
            });

            // 랜덤 뷰포트 + 짧은 지터
            page.setViewportSize(360 + rnd(0, 80), 740 + rnd(0, 180));
            page.navigate(url, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
            page.waitForLoadState(LoadState.NETWORKIDLE);
            page.waitForTimeout(rnd(250, 900));

            // [ADD] 더보기/원문보기 등 모든 토글 시도(페이지/iframe 모두)
            expandAll(page);

            // [ADD] 문단 렌더 안정화(문단 노드가 늦게 붙는 케이스)
            page.waitForSelector("p.se-text-paragraph, " + NaverSelectors.LEGACY_BODY,
                    new Page.WaitForSelectorOptions().setTimeout(2000).setState(WaitForSelectorState.ATTACHED));

            // [CHG] 본문 컨테이너 확보(모바일 → 레거시 → mainFrame)
            Locator body = requireBody(page);

            String title = extractTitle(page).trim();
            String html = body.innerHTML();
            // [FIX] 전체 텍스트 대신 "텍스트성 문단"만 DOM 순서대로 수집
            String text = extractFullText(body);

            // 너무 짧으면 한 번 더 스크롤 후 재수집
            if (text.length() < 50) {
                autoScrollLight(page);
                html = body.innerHTML();
                text = extractFullText(body); // [FIX] 동일 로직 재사용
            }

            if (html.isBlank() || text.isBlank())
                throw new IllegalStateException("본문 선택자/텍스트 추출 실패");

            log.info("NAVER_BLOG crawled url={} textLen={}", url, text.length());
            log.debug("NAVER_BLOG result url={} title='{}' htmlLen={} textLen={} sample=\"{}\"",
                    url, compact(title), html.length(), text.length(), sample(text, 160));

            return new NaverBlogContentRes(title, html, text);
        } finally {
            page.close();
        }
    }

    // --- helpers ---

    private Locator requireBody(Page page) {
        Locator se = page.locator(NaverSelectors.SE_MAIN);
        if (se.count() > 0) return se.first();

        Locator legacy = page.locator(NaverSelectors.LEGACY_BODY);
        if (legacy.count() > 0) return legacy.first();

        // [ADD] 데스크톱 구형 iframe(mainFrame)
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

    // [ADD] 본문 토글을 반복적으로 전부 펼침(가능한 한)
    private void expandAll(Page p) {
        // 1) 페이지 전역
        for (int round = 0; round < 4; round++) {
            int clicked = clickAllOnce(p::locator, NaverSelectors.EXPANDERS); // [FIX] 메서드 레퍼런스 사용
            if (clicked == 0) break;
            p.waitForTimeout(180);
        }
        // 2) mainFrame 내부도 동일 적용
        Frame main = null;
        for (Frame f : p.frames()) {
            if ("mainFrame".equals(f.name())) { main = f; break; }
        }
        if (main != null) {
            for (int round = 0; round < 4; round++) {
                int clicked = clickAllOnce(main::locator, NaverSelectors.EXPANDERS); // [FIX]
                if (clicked == 0) break;
                p.waitForTimeout(180);
            }
        }
    }

    // [FIX] Page/Frame 공통: locator 펑션을 받아 처리
    private int clickAllOnce(Function<String, Locator> finder, String[] selectors) {
        int clicks = 0;
        for (String sel : selectors) {
            Locator list = finder.apply(sel);
            int count = list.count();
            for (int i = 0; i < count && i < 20; i++) { // 과도한 클릭 방지
                Locator el = list.nth(i);
                try {
                    if (el.isVisible()) {
                        el.click(new Locator.ClickOptions().setTimeout(1500));
                        clicks++;
                    }
                } catch (PlaywrightException e) {
                    log.debug("expand ignore sel={} #{} msg={}", sel, i, compact(e.getMessage()));
                }
            }
        }
        return clicks;
    }

    // [FIX] "텍스트성 문단"만 DOM 순서대로 수집 + 비텍스트 조상(이미지/갤러리 등) 제외
    private String extractFullText(Locator body) {
        String js =
                "root => {" +
                        "  const textSel = `" + NaverSelectors.SE_TEXT_NODES + "`;" +             // [ADD]
                        "  const nonTextAncSel = `" + NaverSelectors.SE_NON_TEXT_COMPONENTS + "`;" + // [ADD]
                        "  const nodes = Array.from(root.querySelectorAll(textSel));" +
                        "  const parts = [];" +
                        "  for (const n of nodes) {" +
                        "    if (n.closest(nonTextAncSel)) continue;" + // [ADD] 비텍스트 컴포넌트 하위 배제
                        "    const t = (n.innerText || '').trim();" +
                        "    if (t) parts.push(t);" +
                        "  }" +
                        "  return parts;" +
                        "}";

        @SuppressWarnings("unchecked")
        List<String> parts = (List<String>)body.evaluate(js); // [ADD]

        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            String cleaned = cleanParagraph(part);
            if (isThrowaway(cleaned)) continue; // [ADD]
            if (sb.length() > 0) sb.append("\n\n");
            sb.append(cleaned);
        }

        if (sb.length() > 0) return sb.toString();

        // [CHG] 폴백: 컴포넌트 매칭이 적을 때 컨테이너 전체 텍스트 사용
        return stripPlaceholders(cleanParagraph(body.innerText()));
    }

    // [ADD] 이미지/플레이스홀더/제로폭 문자 제거 규칙
    private static boolean isThrowaway(String s) {
        if (s == null) return true;
        String t = s
                .replace("\u200B", "")
                .replace("\u200C", "")
                .replace("\u200D", "")
                .replace("\uFEFF", "")
                .trim();
        if (t.isBlank()) return true;
        if (t.equals("존재하지 않는 이미지입니다.")) return true;
        return false;
    }

    // [ADD] 이미지/광고/공백성 플레이스홀더 제거
    private static String stripPlaceholders(String s) {
        if (s == null) return "";
        String t = s
                .replace("\u200B", "")
                .replace("\u200C", "")
                .replace("\u200D", "")
                .replace("\uFEFF", "");
        t = t.replaceAll("(?m)^\\s*존재하지 않는 이미지입니다\\.?\\s*$\\n?", "");
        return t.trim();
    }

    // 선택적 클릭(남겨둠)
    private void clickIfPresent(Page p, String sel, int timeoutMs) {
        Locator list = p.locator(sel);
        if (list.count() == 0) return;
        Locator first = list.first();
        try {
            if (first.isVisible()) {
                first.click(new Locator.ClickOptions().setTimeout(timeoutMs));
                p.waitForTimeout(150);
            } else {
                log.debug("click skip sel={} reason=not-visible", sel);
            }
        } catch (PlaywrightException e) {
            log.debug("click ignore sel={} msg={}", sel, compact(e.getMessage()));
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

    private static int rnd(int a, int b) { return ThreadLocalRandom.current().nextInt(a, b + 1); }
    private static String sample(String s, int n) {
        if (s == null) return "";
        String one = s.replaceAll("\\s+", " ").trim();
        return one.substring(0, Math.min(one.length(), n));
    }
    private static String compact(String s) { return s == null ? "" : s.replaceAll("\\s+", " ").trim(); }

    private String toMobileUrl(String url) {
        return url.replace("://blog.naver.com/", "://m.blog.naver.com/");
    }

    // [CHG] 문단 클린업 강화
    private static String cleanParagraph(String s) {
        if (s == null) return "";
        String t = s.replace("\u00A0", " ");
        t = t.replaceAll("[ \\t\\f\\v]+", " ");
        t = t.replaceAll("\\n{3,}", "\n\n");
        return t.trim();
    }
}
