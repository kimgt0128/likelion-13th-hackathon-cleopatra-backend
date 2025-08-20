package com.likelion.cleopatra.domain.openApi.rtms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RtmsRes {
    private String lawdCd;
    private String anchorYm;
    private int months;

    private String amountUnit;       // "만원"
    private String areaUnit;         // "㎡"
    private String unitPriceSqmUnit; // "만원/㎡"
    private String unitPricePUnit;   // "만원/평"

    private double thresholdPyeong;  // 30.0
    private double thresholdSqm;     // 99.17355

    private long smallCount;
    private long largeCount;

    private double smallAvgAmount;    // 만원
    private double largeAvgAmount;    // 만원
    private double smallMedianAmount; // 만원
    private double largeMedianAmount; // 만원

    private double smallAvgPerSqm;    // 만원/㎡
    private double largeAvgPerSqm;    // 만원/㎡
    private double smallAvgPerPyeong; // 만원/평
    private double largeAvgPerPyeong; // 만원/평
}