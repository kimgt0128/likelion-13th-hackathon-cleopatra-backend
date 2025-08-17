package com.likelion.cleopatra.domain.crwal.selector;

public final class NaverPlaceSelectors {
    private NaverPlaceSelectors(){}

    // 방문자 리뷰 카드(더보기/접기 앵커가 있는 li)
    public static final String REVIEW_CARD = "li:has([data-pui-click-code='rvshowless'])";
    public static final String REVIEW_TEXT_MORE = "a[data-pui-click-code='rvshowmore']";
    public static final String REVIEW_TEXT = "div:has(a[data-pui-click-code='rvshowless'])";

    public static final String VISIT_KEYWORDS = "a[data-pui-click-code='visitkeyword'] em";
    public static final String TAG_CHIPS_PARENT = "div:has(img)";
    public static final String REVISIT_CONTAINS = "번째 방문";
}
