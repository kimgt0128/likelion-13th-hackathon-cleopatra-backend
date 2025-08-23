package com.likelion.cleopatra.domain.report.dto.report;

import com.likelion.cleopatra.domain.report.entity.Report;
import lombok.Builder;
import lombok.Getter;

//@AllArgsConstructor
//@NoArgsConstructor
@Builder
@Getter
public class ReportRes {


    public static ReportRes from(Report report) {
        return ReportRes.builder().build();
    }
}
