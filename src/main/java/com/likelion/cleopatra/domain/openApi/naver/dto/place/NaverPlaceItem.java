package com.likelion.cleopatra.domain.openApi.naver.dto.place;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NaverPlaceItem {
    private String title;        // 업체명 (HTML 태그 포함 가능)
    private String link;         // 네이버 플레이스/상세 링크
    private String category;     // 예: "술집>맥주,호프"
    private String description;  // 설명
    private String telephone;    // 전화번호(빈 문자열일 수 있음)
    private String address;      // 지번 주소
    private String roadAddress;  // 도로명 주소
    private String mapx;         // WGS84 x 좌표(문자열로 제공)
    private String mapy;         // WGS84 y 좌표(문자열로 제공)
}
