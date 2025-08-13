package com.likelion.cleopatra.domain.crwal.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 네이버 블로그 크롤링 결과 DTO
 * - 제목
 * - HTML 원문
 * - 가공된 텍스트
 */
@Getter
@AllArgsConstructor
@ToString
public class NaverBlogCrawlRes {

    /** 블로그 글 제목 */
    private final String title;

    /** 본문 HTML */
    private final String html;

    /** HTML에서 추출한 순수 텍스트 */
    private final String text;

}
