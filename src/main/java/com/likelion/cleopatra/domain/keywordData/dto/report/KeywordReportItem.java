package com.likelion.cleopatra.domain.keywordData.dto.report;

import com.likelion.cleopatra.global.common.enums.Platform;
import lombok.*;

import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class KeywordReportItem {
    private Platform platform;     // NAVER_BLOG | NAVER_PLACE | YOUTUBE
    private List<String> keywords; // ["중고 거래", ...]
    private String descript;       // 간단 해설
}