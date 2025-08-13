package com.likelion.cleopatra.domain.crwal.selector;

public final class NaverSelectors {
    private NaverSelectors() {}
    // 스마트에디터3 모바일 뷰
    public static final String SE_MAIN = "div.se-main-container";
    public static final String SE_TEXT_BLOCKS = "div.se-main-container .se-component.se-text";
    // 구버전 본문
    public static final String LEGACY_BODY = "#postViewArea, #postContent, #postViewArea > div";
    // 제목 후보
    public static final String TITLE = "h3.se_textarea, .pcol1, .se_title, meta[property='og:title']";
}