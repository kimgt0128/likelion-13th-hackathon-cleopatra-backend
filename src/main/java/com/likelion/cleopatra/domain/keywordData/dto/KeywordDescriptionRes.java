package com.likelion.cleopatra.domain.keywordData.dto;

import lombok.*;

import java.util.Map;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class KeywordDescriptionRes {
    private String area;
    private String keyword;

    // ex) "data_naver_blog", "data_naver_place", "data_youtube"
    private Map<String, PlatformBlock> data;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PlatformBlock {
        private String platform;               // NAVER_BLOG, NAVER_PLACE, YOUTUBE
        private java.util.List<String> platform_keyword;
        private String platform_description;
    }
}