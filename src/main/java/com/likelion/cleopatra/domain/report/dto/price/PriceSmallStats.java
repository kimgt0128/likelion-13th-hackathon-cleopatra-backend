// src/main/java/com/likelion/cleopatra/domain/report/dto/PriceSmallStats.java
package com.likelion.cleopatra.domain.report.dto.price;

import lombok.*;

/** 소형=50평 이상 집계 결과 */
@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class PriceSmallStats {
    private long count;      // 표본수
    private double average;  // 평균 거래가(만원)
    private double median;   // 중위값(만원)
}
