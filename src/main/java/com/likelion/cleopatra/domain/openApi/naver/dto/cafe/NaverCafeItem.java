package com.likelion.cleopatra.domain.openApi.naver.dto.cafe;

import lombok.Getter;
import lombok.Setter;

/**
 * 개별 카페글 정보
 */
@Getter
@Setter
public class NaverCafeItem {
    /** 카페 게시글 제목 */
    private String title;

    /** 카페 게시글 URL */
    private String link;

    /** 카페 게시글 요약(설명) */
    private String description;

    /** 게시글이 있는 카페 이름 */
    private String cafename;

    /** 게시글이 있는 카페의 URL */
    private String cafeurl;
}