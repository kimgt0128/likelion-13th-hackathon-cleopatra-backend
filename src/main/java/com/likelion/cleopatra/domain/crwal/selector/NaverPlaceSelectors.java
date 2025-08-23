// selector/NaverPlaceSelectors.java
package com.likelion.cleopatra.domain.crwal.selector;

/** 네이버 지도 검색 셀렉터 모음 (iframe/무-iframe 동시 대응) */
public final class NaverPlaceSelectors {
    private NaverPlaceSelectors() {}

    // 공통 배너
    public static final String BANNER_ACCEPT = "button:has-text('동의'), button:has-text('확인')";

    // 검색 리스트(iframe)
    public static final String SEARCH_LIST_CONTAINER = "#_pcmap_list_scroll_container";
    public static final String SEARCH_LIST_ITEMS     = "#_pcmap_list_scroll_container ul > li";
    public static final String SEARCH_CARD_LINK      = "a.place_bluelink";
    // 상호명 노드: 가끔 TYaxT, 가끔 YwYLL
    public static final String SEARCH_CARD_NAME      = "a.place_bluelink span.TYaxT, a.place_bluelink span.YwYLL";

    // 검색 리스트(무-iframe)
    public static final String CARD_ANCHORS_NO_IFRAME =
            "a.place_bluelink, a[href*='place.naver.com/restaurant/'], a[href*='place.naver.com/place']";
    public static final String PLACE_TAB   = "button[role='tab']:has-text('장소'), a[role='tab']:has-text('장소')";
    public static final String SEARCH_MORE = "a:has-text('검색결과 더보기'), button:has-text('검색결과 더보기'), a:has-text('더보기')";
}
