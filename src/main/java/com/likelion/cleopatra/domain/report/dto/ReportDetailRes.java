package com.likelion.cleopatra.domain.report.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion.cleopatra.domain.report.entity.Report;
import com.likelion.cleopatra.global.common.enums.keyword.Secondary;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportDetailRes {

    @JsonProperty("report_id")
    private Long reportId;

    @JsonProperty("secondary")
    private Secondary secondary;

    @JsonProperty("sub_neighborhood")
    private String subNeighborhood;

    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @Builder.Default
    @JsonProperty("favorite")
    private boolean favorite = false;

    // 섹션
    @JsonProperty("description_summary")
    private JsonNode descriptionSummary;

    @JsonProperty("keywords")
    private JsonNode keywords;

    @JsonProperty("population")
    private JsonNode population;

    @JsonProperty("price")
    private JsonNode price;

    @JsonProperty("income_consumption")
    private JsonNode incomeConsumption;

    @JsonProperty("description_strategy")
    private JsonNode descriptionStrategy;

    public static ReportDetailRes from(Report r, ObjectMapper om) {
        return ReportDetailRes.builder()
                .reportId(r.getId())
                .secondary(r.getSecondary())
                .subNeighborhood(r.getSubNeighborhood())
                .createdAt(r.getCreatedAt())
                .descriptionSummary(read(om, r.getDescriptionSummaryJson()))
                .keywords(read(om, r.getKeywordsJson()))
                .population(read(om, r.getPopulationJson()))
                .price(read(om, r.getPriceJson()))
                .incomeConsumption(read(om, r.getIncomeConsumptionJson()))
                .descriptionStrategy(read(om, r.getDescriptionStrategyJson()))
                .build();
    }

    private static JsonNode read(ObjectMapper om, String json) {
        if (json == null) return null;
        try { return om.readTree(json); }
        catch (Exception e) { throw new IllegalStateException("JSON parse failed", e); }
    }
}