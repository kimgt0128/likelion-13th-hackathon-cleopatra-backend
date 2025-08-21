package com.likelion.cleopatra.domain.report.dto;

import com.likelion.cleopatra.domain.report.entity.Report;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

//@AllArgsConstructor
//@NoArgsConstructor
@Builder
@Getter
public class ReportRes {


    public static ReportRes from(Report report) {
        return ReportRes.builder().build();
    }
}
