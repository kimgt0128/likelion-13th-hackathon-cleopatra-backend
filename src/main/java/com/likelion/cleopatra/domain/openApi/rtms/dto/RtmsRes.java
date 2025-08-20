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
    private String lawdCd;           // 예: 11110
    private String anchorYm;         // 기준월(YYYYMM), 예: 202505
    private int months;              // 집계 개월수(기본 12)

    private String amountUnit;       // "만원"
    private String areaUnit;         // "㎡"
    private double thresholdPyeong;  // 30.0
    private double thresholdSqm;     // 99.17355

    private long smallCount;         // 소형 표본수
    private long largeCount;         // 대형 표본수
    private double smallAvgAmount;   // 소형 평균거래가(만원)
    private double largeAvgAmount;   // 대형 평균거래가(만원)
}