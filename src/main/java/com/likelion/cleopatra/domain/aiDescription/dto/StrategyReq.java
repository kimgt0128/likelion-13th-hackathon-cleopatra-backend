package com.likelion.cleopatra.domain.aiDescription.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StrategyReq {
    private String area;       // "노원구 공릉동"
    private String category;   // "외식업 한식"

    // data_naver_blog / data_naver_place / data_youtube
    private Map<String, PlatformBlock> data;

    private Population population;
    private Price price;

    @JsonProperty("income_consumption")
    private IncomeConsumption incomeConsumption;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PlatformBlock {
        private String platform;
        @JsonProperty("platform_keyword")
        private List<String> platformKeyword;
        @JsonProperty("platform_description")
        private String platformDescription;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Population {
        @JsonProperty("total_resident") private Integer totalResident;
        private Ages ages;
        private Gender gender;

        @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
        public static class Ages {
            private Resident resident;
            private Percent percent;
            @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
            public static class Resident {
                @JsonProperty("age_10_resident") private Integer age10Resident;
                @JsonProperty("age_20_resident") private Integer age20Resident;
                @JsonProperty("age_30_resident") private Integer age30Resident;
                @JsonProperty("age_40_resident") private Integer age40Resident;
                @JsonProperty("age_50_resident") private Integer age50Resident;
                @JsonProperty("age_60_plus_resident") private Integer age60PlusResident;
            }
            @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
            public static class Percent {
                @JsonProperty("age_10_percent") private Double age10Percent;
                @JsonProperty("age_20_percent") private Double age20Percent;
                @JsonProperty("age_30_percent") private Double age30Percent;
                @JsonProperty("age_40_percent") private Double age40Percent;
                @JsonProperty("age_50_percent") private Double age50Percent;
                @JsonProperty("age_60_plus_percent") private Double age60PlusPercent;
            }
        }
        @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
        public static class Gender {
            private Resident resident;
            private Percent percent;
            @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
            public static class Resident {
                @JsonProperty("male_resident") private Integer maleResident;
                @JsonProperty("female_resident") private Integer femaleResident;
            }
            @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
            public static class Percent {
                @JsonProperty("male_percent") private Double malePercent;
                @JsonProperty("female_percent") private Double femalePercent;
            }
        }
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Price {
        private Big big;
        private Small small;
        @JsonProperty("price_per_meter") private Integer pricePerMeter;
        @JsonProperty("price_per_pyeong") private Integer pricePerPyeong;
        @JsonProperty("trading_volume") private Map<String, Integer> tradingVolume;

        @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
        public static class Big   { @JsonProperty("big_average")  private Integer bigAverage;  @JsonProperty("big_middle")  private Integer bigMiddle;  @JsonProperty("big_count")  private Integer bigCount; }
        @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
        public static class Small { @JsonProperty("small_average") private Integer smallAverage; @JsonProperty("small_middle") private Integer smallMiddle; @JsonProperty("small_count") private Integer smallCount; }
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class IncomeConsumption {
        private Income income;
        private Consumption consumption;
        @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
        public static class Income { @JsonProperty("monthly_income_average") private Integer monthlyIncomeAverage; @JsonProperty("income_class_code") private String incomeClassCode; }
        @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
        public static class Consumption {
            @JsonProperty("spending_total") private Integer spendingTotal;
            private Expend expend;
            private Percent percent;
            @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
            public static class Expend {
                private Integer food; @JsonProperty("clothing_footwear") private Integer clothingFootwear;
                @JsonProperty("living_goods") private Integer livingGoods; private Integer medical; private Integer transport; private Integer education;
                @JsonProperty("leisure_culture") private Integer leisureCulture; private Integer other; @JsonProperty("eating_out") private Integer eatingOut; private Integer entertainment;
            }
            @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
            public static class Percent {
                private Double food; @JsonProperty("clothing_footwear") private Double clothingFootwear;
                @JsonProperty("living_goods") private Double livingGoods; private Double medical; private Double transport; private Double education;
                @JsonProperty("leisure_culture") private Double leisureCulture; private Double other; @JsonProperty("eating_out") private Double eatingOut; private Double entertainment;
            }
        }
    }
}