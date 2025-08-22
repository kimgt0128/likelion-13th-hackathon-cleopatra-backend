package com.likelion.cleopatra.domain.incomeConsumption.dto.income;

import com.likelion.cleopatra.domain.incomeConsumption.document.IncomeConsumptionDoc;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Income {
    private BigDecimal monthlyIncomeAverage;
    private String incomeClassCode;

    public static Income from(IncomeConsumptionDoc doc) {
        return Income.builder()
                .monthlyIncomeAverage(doc.getMonthlyIncomeAvg())
                .incomeClassCode(doc.getIncomeClassCode()).build();
    }
}