// src/main/java/com/likelion/cleopatra/domain/aiDescription/dto/ReportDescription.java
package com.likelion.cleopatra.domain.aiDescription.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ReportDescription {
    private DescriptionSummary descriptionSummary;
    private DescriptionPopulation descriptionPopulation;
    private DescriptionPrice descriptionPrice;
    private String incomeConsumptionDescription;
    private DescriptionStrategy descriptionStrategy;

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class DescriptionSummary {
        private String totalDescription;
        private String line1;   // line_1
        private String line2;   // line_2
        private String line3;   // line_3
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class DescriptionPopulation {
        private String age;
        private String gender;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class DescriptionPrice {
        private String valueAverage; // value_average
        private String valuePyeong;  // value_pyeong
        private String volume;
    }

    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
    public static class DescriptionStrategy {
        private Block review;
        private Block kpi;
        private Block improvements;

        @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
        @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
        public static class Block {
            private String head;
            private java.util.List<String> body;
        }
    }
}
