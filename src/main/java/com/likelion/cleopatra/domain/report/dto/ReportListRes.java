package com.likelion.cleopatra.domain.report.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.likelion.cleopatra.domain.report.entity.Report;
import com.likelion.cleopatra.global.common.enums.keyword.Secondary;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportListRes {

    private long reportCount;

    private List<ReportInfo> reportList;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ReportInfo {
        private Long reportId;

        @JsonProperty("secondary")
        private Secondary secondary;

        private String subNeighborhood;

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;

        @Builder.Default
        private boolean favorite = false;

        public static ReportInfo from(Report r) {
            return ReportInfo.builder()
                    .reportId(r.getId())
                    .secondary(r.getSecondary())
                    .subNeighborhood(
                            r.getSubNeighborhood() == null ? null : r.getSubNeighborhood().toString()
                    )
                    .createdAt(r.getCreatedAt())
                    .build();
        }
    }

    public static ReportListRes from(List<Report> reports) {
        return ReportListRes.builder()
                .reportCount(reports == null ? 0 : reports.size())
                .reportList(reports == null ? List.of()
                        : reports.stream().map(ReportInfo::from).toList())
                .build();
    }
}
