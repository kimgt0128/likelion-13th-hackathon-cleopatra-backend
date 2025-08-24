package com.likelion.cleopatra.domain.incomeConsumption.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.likelion.cleopatra.domain.incomeConsumption.dto.consumption.Consumption;
import com.likelion.cleopatra.domain.incomeConsumption.dto.description.DescriptionIncomeConsumption;
import com.likelion.cleopatra.domain.incomeConsumption.dto.income.Income;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncomeConsumptionRes {
    private Income income;         // 소득
    private Consumption consumption; // 지출

    public static IncomeConsumptionRes from(Income income, Consumption consumption) {
        return IncomeConsumptionRes.builder()
                .income(income)
                .consumption(consumption).build();
    }
}
