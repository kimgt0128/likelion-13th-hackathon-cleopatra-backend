package com.likelion.cleopatra.domain.report.dto.report;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.likelion.cleopatra.domain.report.entity.Report;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 엔티티(Report) -> 응답 DTO
 * - 섹션 필드는 JsonNode로 직렬화하여 프론트에 그대로 내려줌
 * - 스네이크 케이스로 출력되도록 설정(report_id, description_summary 등)
 */
@Getter
@Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ReportRes {

    // 메타
    private final Long reportId;
    private final String primary;
    private final String secondary;
    private final String district;
    private final String neighborhood;
    private final String subNeighborhood;
    private final LocalDateTime createdAt;

    // 섹션(JSON 그대로)
    private final JsonNode descriptionSummary;   // $.description_summary
    private final JsonNode keywords;             // $.keywords
    private final JsonNode population;           // $.population
    private final JsonNode price;                // $.price
    private final JsonNode incomeConsumption;    // $.income_consumption
    private final JsonNode descriptionStrategy;  // $.description_strategy

    public static ReportRes from(Report report) {
        ObjectMapper om = new ObjectMapper();
        return ReportRes.builder()
                // 메타
                .reportId(report.getId())
                .primary(enumText(report.getPrimary()))
                .secondary(enumText(report.getSecondary()))
                .district(enumText(report.getDistrict()))
                .neighborhood(enumText(report.getNeighborhood()))
                .subNeighborhood(report.getSubNeighborhood())
                .createdAt(report.getCreatedAt())
                // 섹션(JSON 파싱)
                .descriptionSummary(readTree(om, report.getDescriptionSummaryJson()))
                .keywords(readTree(om, report.getKeywordsJson()))
                .population(readTree(om, report.getPopulationJson()))
                .price(readTree(om, report.getPriceJson()))
                .incomeConsumption(readTree(om, report.getIncomeConsumptionJson()))
                .descriptionStrategy(readTree(om, report.getDescriptionStrategyJson()))
                .build();
    }

    private static JsonNode readTree(ObjectMapper om, String json) {
        if (json == null || json.isBlank()) return null;
        try { return om.readTree(json); }
        catch (Exception e) { return null; }
    }

    /** enum -> 표시 문자열(가능하면 getKo(), 없으면 name()) */
    private static String enumText(Enum<?> e) {
        if (e == null) return null;
        try {
            var m = e.getClass().getMethod("getKo");
            Object v = m.invoke(e);
            if (v instanceof String s && !s.isBlank()) return s;
        } catch (Exception ignore) {}
        return e.name();
    }
}
