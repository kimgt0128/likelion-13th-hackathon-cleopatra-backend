// src/main/java/com/likelion/cleopatra/domain/aiDescription/dto/ReportDescription.java
package com.likelion.cleopatra.domain.aiDescription.dto;

import lombok.*;

/** AI가 생성하는 설명 묶음(데이터 요약 텍스트) */
@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class ReportDescription {
    private DescriptionSummary descriptionSummary;
    private DescriptionPopulation descriptionPopulation;
    private DescriptionPrice descriptionPrice;
    private String incomeConsumptionDescription;   // 단일 문장/문단
    private DescriptionStrategy descriptionStrategy;

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DescriptionSummary {
        private String totalDescription;
        private String line1;
        private String line2;
        private String line3;
    }
    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DescriptionPopulation {
        private String age;
        private String gender;
    }
    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DescriptionPrice {
        private String valueAverage;
        private String valuePyeong;
        private String volume;
    }
    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class DescriptionStrategy {
        private Block review;
        private Block kpi;
        private Block improvements;

        @Getter @Builder @NoArgsConstructor @AllArgsConstructor
        public static class Block {
            private String head;
            private java.util.List<String> body;
        }
    }
}
