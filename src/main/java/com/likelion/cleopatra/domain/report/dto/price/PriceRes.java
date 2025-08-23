// src/main/java/com/likelion/cleopatra/domain/report/dto/price/PriceRes.java
package com.likelion.cleopatra.domain.report.dto.price;

import lombok.*;
import java.util.Map;

/**
 * 가격 섹션 DTO(최종 형식).
 * 단위: 금액=만원, 면적=㎡, 단가=만원/㎡·만원/평
 */
@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class PriceRes {
    private String lawdCd;
    private String anchorYm;      // YYYYMM
    private int months;           // 12

    // 소형=50평 이상
    private long smallCount;
    private double smallAverage;  // 만원
    private double smallMedian;   // 만원

    // 대형=50평 미만
    private long bigCount;
    private double bigAverage;    // 만원
    private double bigMedian;     // 만원

    // 1년치 모든 거래의 단가 평균
    private double pricePerMeter;   // 만원/㎡
    private double pricePerPyeong;  // 만원/평

    // 최근 4분기 거래량: 키 형식 "YYYY_Q_quarter"
    private Map<String, Long> tradingVolume;
}
