package com.likelion.cleopatra.domain.incomeConsumption.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.likelion.cleopatra.domain.incomeConsumption.dto.consumption.Consumption;
import com.likelion.cleopatra.domain.incomeConsumption.dto.income.Income;
import lombok.*;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class IncomeConsumptionRes {
    private Income income;
    @JsonProperty("consumption")
    private ConsumptionView consumption;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ConsumptionView {
        @JsonProperty("spending_total") private BigDecimal spendingTotal; // 만원
        @JsonProperty("expend")  private Map<String, BigDecimal> expend;  // 만원
        @JsonProperty("percent") private Map<String, Double> percent;     // %
    }

    public static IncomeConsumptionRes from(Income income, Consumption c) {
        String[] keys = {
                "food","clothing_footwear","living_goods","medical","transport",
                "education","entertainment","leisure_culture","other","eating_out"
        };

        Map<String, BigDecimal> expendMap  = new LinkedHashMap<>();
        Map<String, Double>     percentMap = new LinkedHashMap<>();

        List<BigDecimal> ex = c.getExpend();
        List<Double>     pr = c.getPercent();

        for (int i = 0; i < keys.length; i++) {
            expendMap.put(keys[i],  i < ex.size() ? ex.get(i) : BigDecimal.ZERO);
            percentMap.put(keys[i], i < pr.size() ? pr.get(i) : 0.0);
        }

        return IncomeConsumptionRes.builder()
                .income(income)
                .consumption(ConsumptionView.builder()
                        .spendingTotal(c.getSpendingTotal())
                        .expend(expendMap)
                        .percent(percentMap) // "spending_total" 퍼센트는 넣지 않음
                        .build())
                .build();
    }
}
