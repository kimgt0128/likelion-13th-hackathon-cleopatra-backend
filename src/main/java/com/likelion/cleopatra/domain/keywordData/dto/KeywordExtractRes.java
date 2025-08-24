// src/main/java/com/likelion/cleopatra/domain/keywordData/dto/KeywordExtractRes.java
package com.likelion.cleopatra.domain.keywordData.dto;

import com.likelion.cleopatra.domain.keywordData.document.KeywordDoc;
import com.likelion.cleopatra.global.common.enums.Platform;
import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class KeywordExtractRes {

    private String area;                   // 예: "노원구 공릉동"
    private String keyword;                // 예: "외식업 일식"
    private int blogCount;                 // NAVER_BLOG 수집 건수
    private int placeCount;                // NAVER_PLACE 수집 건수
    private int youtubeCount;              // YOUTUBE 수집 건수
    private List<PlatformSummary> platforms; // 플랫폼별 키워드/해설

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PlatformSummary {
        private Platform platform;         // NAVER_BLOG | NAVER_PLACE | YOUTUBE
        private List<String> keywords;     // 플랫폼별 키워드
        private String descript;           // 간단 해설
    }

    /** 저장된 플랫폼별 문서 목록으로 응답 구성 */
    public static KeywordExtractRes of(
            String area, String query, List<KeywordDoc> docs,
            int blogCount, int placeCount, int youtubeCount
    ) {
        List<PlatformSummary> ps = (docs == null) ? List.of()
                : docs.stream()
                .map(d -> PlatformSummary.builder()
                        .platform(d.getPlatform())
                        .keywords(d.getKeywords())
                        .descript(d.getDescript())
                        .build())
                .toList();

        return KeywordExtractRes.builder()
                .area(area)
                .keyword(query)
                .blogCount(blogCount)
                .placeCount(placeCount)
                .youtubeCount(youtubeCount)
                .platforms(ps)
                .build();
    }
}
