// src/main/java/com/likelion/cleopatra/domain/aiDescription/dto/ReportDescription.java
package com.likelion.cleopatra.domain.aiDescription.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
        @JsonProperty("line_1") @JsonAlias("line1")
        private String line1;   // line_1
        @JsonProperty("line_2") @JsonAlias("line2")
        private String line2;   // line_2
        @JsonProperty("line_3") @JsonAlias("line3")
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
