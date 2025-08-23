package com.likelion.cleopatra.domain.report.dto.report;

import com.likelion.cleopatra.domain.aiDescription.dto.ReportDescription;
import com.likelion.cleopatra.domain.incomeConsumption.dto.IncomeConsumptionRes;
import com.likelion.cleopatra.domain.keywordData.dto.report.KeywordReportRes;
import com.likelion.cleopatra.domain.population.dto.PopulationRes;
import com.likelion.cleopatra.domain.report.dto.price.PriceRes;
import lombok.*;

/** 엔티티 저장 직전에 사용하는 통합 DTO: 데이터 + 설명 */
@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class TotalReportRes {

    // 데이터 섹션
    private KeywordReportRes keywordReportRes;
    private PopulationRes population;
    private PriceRes price;
    private IncomeConsumptionRes incomeConsumption;

    // 설명 섹션
    private ReportDescription.DescriptionSummary descriptionSummary;
    private ReportDescription.DescriptionPopulation descriptionPopulation;
    private ReportDescription.DescriptionPrice descriptionPrice;
    private String incomeConsumptionDescription;
    private ReportDescription.DescriptionStrategy descriptionStrategy;

    public static TotalReportRes from(ReportData data, ReportDescription desc) {
        return TotalReportRes.builder()
                .keywordReportRes(data.getKeywordReportRes())
                .population(data.getPopulationRes())
                .price(data.getPriceRes())
                .incomeConsumption(data.getIncomeConsumptionRes())
                .descriptionSummary(desc.getDescriptionSummary())
                .descriptionPopulation(desc.getDescriptionPopulation())
                .descriptionPrice(desc.getDescriptionPrice())
                .incomeConsumptionDescription(desc.getIncomeConsumptionDescription())
                .descriptionStrategy(desc.getDescriptionStrategy())
                .build();
    }
}
