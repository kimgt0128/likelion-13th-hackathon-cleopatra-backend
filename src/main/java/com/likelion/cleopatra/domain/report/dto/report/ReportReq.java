// src/main/java/com/likelion/cleopatra/domain/report/dto/ReportReq.java
package com.likelion.cleopatra.domain.report.dto.report;

import com.likelion.cleopatra.global.common.enums.address.District;
import com.likelion.cleopatra.global.common.enums.address.Neighborhood;
import com.likelion.cleopatra.global.common.enums.keyword.Primary;
import com.likelion.cleopatra.global.common.enums.keyword.Secondary;
import lombok.*;

/** 보고서 생성 요청 DTO. 클라이언트 바디를 그대로 매핑한다. */
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ReportReq {
    private Primary primary;          // 업종 대분류
    private Secondary secondary;        // 업종 소분류
    private District district;         // 시군구(예: 노원구)
    private Neighborhood neighborhood;     // 법정동 대분류(예: 공릉동)
    private String sub_neighborhood; // 행정동(예: 공릉 1동)
}
