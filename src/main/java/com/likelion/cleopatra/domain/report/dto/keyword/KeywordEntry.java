package com.likelion.cleopatra.domain.report.dto.keyword;

import lombok.*;

import java.util.List;

/** 플랫폼별 키워드 묶음 */
@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class KeywordEntry {
    private String platform;        // NAVER_BLOG, NAVER_REVIEW, YOUTUBE
    private List<String> keywords;  // ["중고 거래", ...]
    private String descript;        // 간단 설명
}