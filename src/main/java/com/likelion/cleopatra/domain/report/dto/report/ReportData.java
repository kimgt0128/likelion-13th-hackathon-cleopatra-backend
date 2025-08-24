package com.likelion.cleopatra.domain.report.dto.report;

import com.likelion.cleopatra.domain.incomeConsumption.dto.IncomeConsumptionRes;
import com.likelion.cleopatra.domain.keywordData.dto.report.KeywordReportRes;
import com.likelion.cleopatra.domain.population.dto.PopulationRes;
import com.likelion.cleopatra.domain.report.dto.price.PriceRes;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class ReportData {

    private KeywordReportRes keywordReportRes;
    private PopulationRes populationRes;
    private PriceRes priceRes;
    private IncomeConsumptionRes incomeConsumptionRes;

    public static ReportData of(KeywordReportRes keywordReportRes,PopulationRes populationRes, PriceRes priceRes, IncomeConsumptionRes incomeConsumptionRes) {

        return ReportData.builder()
                .keywordReportRes(keywordReportRes)
                .populationRes(populationRes)
                .priceRes(priceRes)
                .incomeConsumptionRes(incomeConsumptionRes).build();
    }
}
