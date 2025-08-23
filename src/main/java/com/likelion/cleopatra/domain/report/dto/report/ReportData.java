package com.likelion.cleopatra.domain.report.dto.report;

import com.likelion.cleopatra.domain.incomeConsumption.dto.IncomeConsumptionRes;
import com.likelion.cleopatra.domain.population.dto.PopulationRes;
import com.likelion.cleopatra.domain.report.dto.keyword.KeywordEntry;
import com.likelion.cleopatra.domain.report.dto.price.PriceRes;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class ReportData {

    private PopulationRes populationRes;
    private PriceRes priceRes;
    private IncomeConsumptionRes incomeConsumptionRes;
    private List<KeywordEntry> keywords;

    public static ReportData of(PopulationRes populationRes, PriceRes priceRes, IncomeConsumptionRes incomeConsumptionRes, List<KeywordEntry> keywords) {

        return ReportData.builder()
                .populationRes(populationRes)
                .populationRes(populationRes)
                .incomeConsumptionRes(incomeConsumptionRes)
                .keywords(keywords).build();
    }
}
