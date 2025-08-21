// src/main/java/com/likelion/cleopatra/domain/report/dto/PriceBigStats.java
package com.likelion.cleopatra.domain.report.dto.price;

import lombok.*;

/** 대형=50평 미만 집계 결과 */
@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class PriceBigStats {
    private long count;      // 표본수
    private double average;  // 평균 거래가(만원)
    private double median;   // 중위값(만원)
}
