// src/main/java/com/likelion/cleopatra/domain/report/dto/ReportReq.java
package com.likelion.cleopatra.domain.report.dto;

import lombok.*;

/** 보고서 생성 요청 DTO. 클라이언트 바디를 그대로 매핑한다. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReportReq {
    private String primary;          // 업종 대분류
    private String secondary;        // 업종 소분류
    private String district;         // 시군구(예: 노원구)
    private String neighborhood;     // 법정동 대분류(예: 공릉동)
    private String sub_neighborhood; // 행정동(예: 공릉 1동)
}
