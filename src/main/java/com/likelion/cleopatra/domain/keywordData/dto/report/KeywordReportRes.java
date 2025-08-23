package com.likelion.cleopatra.domain.keywordData.dto.report;

import com.likelion.cleopatra.domain.keywordData.document.KeywordDoc;
import lombok.*;

import java.util.Comparator;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class KeywordReportRes {
    private List<KeywordReportItem> keywords; // 보고서에 들어갈 키워드 섹션

    public static KeywordReportRes fromDocs(List<KeywordDoc> docs) {
        if (docs == null || docs.isEmpty()) {
            return KeywordReportRes.builder().keywords(List.of()).build();
        }
        List<KeywordReportItem> items = docs.stream()
                .sorted(Comparator.comparing(d -> d.getPlatform().name()))
                .map(d -> KeywordReportItem.builder()
                        .platform(d.getPlatform())
                        .keywords(d.getKeywords())
                        .descript(d.getDescript())
                        .build())
                .toList();
        return KeywordReportRes.builder().keywords(items).build();
    }
}