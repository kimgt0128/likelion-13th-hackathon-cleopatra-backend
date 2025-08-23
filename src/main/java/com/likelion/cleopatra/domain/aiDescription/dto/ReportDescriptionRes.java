package com.likelion.cleopatra.domain.aiDescription.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class ReportDescriptionRes {

    private DescriptionSummary descriptionSummary;
    private DescriptionPopulation descriptionPopulation;
    private DescriptionPrice descriptionPrice;
    private DescriptionIncomeCunsumption descriptionIncomeCunsumption;
    private DescriptionStrategy descriptionStrategy;

    public static ReportDescriptionRes from(
            DescriptionSummary descriptionSummary,
            DescriptionPopulation descriptionPopulation,
            DescriptionPrice descriptionPrice,
            DescriptionIncomeCunsumption descriptionIncomeCunsumption,
            DescriptionStrategy descriptionStrategy
            )
    {
        return ReportDescriptionRes.builder()
                .descriptionSummary(descriptionSummary)
                .descriptionPopulation(descriptionPopulation)
                .descriptionPrice(descriptionPrice)
                .descriptionIncomeCunsumption(descriptionIncomeCunsumption)
                .descriptionStrategy(descriptionStrategy)
                .build();
    }

}
