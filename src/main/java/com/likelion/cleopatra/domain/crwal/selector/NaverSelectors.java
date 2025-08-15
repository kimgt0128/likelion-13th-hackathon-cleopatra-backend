package com.likelion.cleopatra.domain.crwal.selector;

public final class NaverSelectors {
    private NaverSelectors() {}

    // 스마트에디터3 모바일 뷰(본문 컨테이너)
    public static final String SE_MAIN = "div.se-main-container";

    // [ADD] 실제 텍스트 노드들(하이픈 표기, DOM 순서 유지)
    public static final String SE_TEXT_NODES =
            "p.se-text-paragraph," +
                    "h1.se-text-h1, h2.se-text-h2, h3.se-text-h3," +
                    "li.se-list-item, blockquote.se-text-quote, pre.se-code, figcaption.se-caption";

    // [ADD] 텍스트성 컴포넌트(보조용, 폴백 추출 시 사용 가능)
    public static final String SE_TEXTUAL_BLOCKS =
            "div.se-main-container .se-component.se-text," +
                    "div.se-main-container .se-component.se-quotation," +
                    "div.se-main-container .se-component.se-heading," +
                    "div.se-main-container .se-component .se_textarea," +
                    "div.se-main-container .se-component .se_paragraph";

    // [ADD] 텍스트 제외할 컴포넌트(조상 기준으로 필터링)
    public static final String SE_NON_TEXT_COMPONENTS =
            ".se-component.se-image, .se-component.se-gallery, .se-component.se-video," +
                    ".se-component.se-map, .se-component.se-oglink, .se-component.se-product," +
                    ".se-component.se-attach, .se-component.se-sticker";

    // 구버전 본문
    public static final String LEGACY_BODY = "#postViewArea, #postContent, #postViewArea > div";

    // 제목 후보
    public static final String TITLE = "h3.se_textarea, .pcol1, .se_title, meta[property='og:title']";

    // [ADD] 더보기/원문보기/접기해제 등 확장 버튼들
    public static final String[] EXPANDERS = new String[] {
            "button:has-text('더보기')",
            ".se-more-button", ".se-more-text", ".se-module-more", ".se-more-less a",
            "a:has-text('원문보기')",
            ".postbtn_more", ".btn_unfold", ".btn_fold"
    };
}
