package com.likelion.cleopatra.domain.keywordData.document;

import com.likelion.cleopatra.global.common.enums.Platform;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "keyword_data")
public class KeywordDoc {

    @Id
    private String id;

    /** 수집 기준 키(예: 검색어) */
    private String keyword;

    /** 플랫폼별 키워드/해설 */
    private List<PlatformKeywords> keywords;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class PlatformKeywords {
        private Platform platform;      // NAVER_BLOG, NAVER_REVIEW, YOUTUBE
        private List<String> keywords;  // ["중고 거래", "과제", "직거래"]
        private String descript;        // 간단 해설
    }
}
