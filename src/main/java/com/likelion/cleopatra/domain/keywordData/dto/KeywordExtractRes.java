package com.likelion.cleopatra.domain.keywordData.dto;

import com.likelion.cleopatra.domain.keywordData.document.KeywordDoc;
import com.likelion.cleopatra.global.common.enums.Platform;
import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class KeywordExtractRes {
    private String area;
    private String keyword;
    private List<PlatformSummary> platforms; // 플랫폼별 요약
    private int blogCount;
    private int placeCount;
    private int youtubeCount;
    private String savedId; // 저장된 KeywordDoc id

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PlatformSummary {
        private Platform platform;
        private List<String> keywords;
        private String descript;
    }

    public static KeywordExtractRes of(
            String area, String query, KeywordDoc doc,
            int blogCount, int placeCount, int youtubeCount
    ) {
        List<PlatformSummary> ps = (doc.getKeywords() == null) ? List.of()
                : doc.getKeywords().stream()
                .map(k -> PlatformSummary.builder()
                        .platform(k.getPlatform())
                        .keywords(k.getKeywords())
                        .descript(k.getDescript())
                        .build())
                .toList();

        return KeywordExtractRes.builder()
                .area(area)
                .keyword(query)
                .platforms(ps)
                .blogCount(blogCount)
                .placeCount(placeCount)
                .youtubeCount(youtubeCount)
                .savedId(doc.getId())
                .build();
    }
}