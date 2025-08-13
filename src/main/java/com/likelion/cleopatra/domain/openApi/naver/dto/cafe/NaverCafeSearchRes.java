package com.likelion.cleopatra.domain.openApi.naver.dto.cafe;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 네이버 카페글 검색 API 응답 DTO
 * JSON 응답을 Jackson이 자동으로 매핑
 */
@Getter
@Setter
public class NaverCafeSearchRes {

    /** 검색 결과 생성 시각 */
    private String lastBuildDate;

    /** 총 검색 결과 개수 */
    private int total;

    /** 검색 시작 위치 */
    private int start;

    /** 한 번에 표시할 검색 결과 개수 */
    private int display;

    /** 카페글 검색 결과 목록 */
    private List<NaverCafeItem> items;
}