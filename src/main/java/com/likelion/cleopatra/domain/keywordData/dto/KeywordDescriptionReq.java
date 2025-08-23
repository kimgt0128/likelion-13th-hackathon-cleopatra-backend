package com.likelion.cleopatra.domain.keywordData.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class KeywordDescriptionReq {

    private String areaa;
    private String keyword;

    // data_naver_blog, data_naver_palce, data_youtube 키로 담는다.
    private Map<String, List<Snippet>> data;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Snippet {
        private String doc_id;
        private String platform;
        private String text;
    }
}