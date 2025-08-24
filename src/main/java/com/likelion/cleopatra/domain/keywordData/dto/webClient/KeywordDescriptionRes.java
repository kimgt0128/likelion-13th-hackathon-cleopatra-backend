package com.likelion.cleopatra.domain.keywordData.dto.webClient;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class KeywordDescriptionRes {
    private String area;
    private String category; // 응답도 카테고리로 정렬할 경우

    // ex) "data_naver_blog", "data_naver_palce", "data_youtube"
    private Map<String, PlatformBlock> data;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PlatformBlock {
        private String platform;                 // NAVER_BLOG, NAVER_PLACE, YOUTUBE
        private List<String> platform_keyword;   // 키워드 리스트
        private String platform_description;     // 플랫폼 설명
    }
}