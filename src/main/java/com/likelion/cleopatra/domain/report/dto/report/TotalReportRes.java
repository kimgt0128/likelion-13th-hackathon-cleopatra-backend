// src/main/java/com/likelion/cleopatra/domain/report/dto/report/TotalReportRes.java
package com.likelion.cleopatra.domain.report.dto.report;

import com.likelion.cleopatra.domain.aiDescription.dto.ReportDescription;
import com.likelion.cleopatra.domain.incomeConsumption.dto.IncomeConsumptionRes;
import com.likelion.cleopatra.domain.keywordData.dto.report.KeywordReportRes;
import com.likelion.cleopatra.domain.population.dto.PopulationRes;
import com.likelion.cleopatra.domain.report.dto.price.PriceRes;
import com.likelion.cleopatra.global.common.enums.Platform;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class TotalReportRes {

    private ReportDescription.DescriptionSummary description_summary;
    private List<KeywordRow> keywords;
    private PopulationOut population;
    private PriceOut price;
    private IncomeConsumptionOut income_consumption;
    private ReportDescription.DescriptionStrategy description_strategy;

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class KeywordRow {
        private String platform;
        private List<String> keywords;
        private String descript;
    }

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PopulationOut {
        private long total_resident;
        private AgesOut ages;
        private GenderOut gender;
        private ReportDescription.DescriptionPopulation description_population;
    }

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class AgesOut {
        private AgesResident resident;
        private AgesPercent percent;
    }

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class AgesResident {
        private long age_10_resident;
        private long age_20_resident;
        private long age_30_resident;
        private long age_40_resident;
        private long age_50_resident;
        private long age_60_plus_resident;
    }

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class AgesPercent {
        private double age_10_percent;
        private double age_20_percent;
        private double age_30_percent;
        private double age_40_percent;
        private double age_50_percent;
        private double age_60_plus_percent;
    }

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class GenderOut {
        private GenderResident resident;
        private GenderPercent percent;
    }

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class GenderResident {
        private long male_resident;
        private long female_resident;
    }

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class GenderPercent {
        private double male_percent;
        private double female_percent;
    }

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PriceOut {
        private PriceBig big;
        private PriceSmall small;
        private double price_per_meter;
        private double price_per_pyeong;
        private Map<String, Long> trading_volume;
        private ReportDescription.DescriptionPrice description_price;
    }

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PriceBig {
        private double big_average;
        private double big_middle;
        private long big_count;
    }

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PriceSmall {
        private double small_average;
        private double small_middle;
        private long small_count;
    }

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class IncomeConsumptionOut {
        private IncomeOut income;
        private ConsumptionOut consumption;
        private String income_consumption_description;
    }

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class IncomeOut {
        private BigDecimal monthly_income_average;
        private String income_class_code;
    }

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ConsumptionOut {
        private BigDecimal spending_total;
        private ExpendOut expend;
        private PercentOut percent;
    }

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class ExpendOut {
        private BigDecimal food;
        private BigDecimal clothing_footwear;
        private BigDecimal living_goods;
        private BigDecimal medical;
        private BigDecimal transport;
        private BigDecimal education;
        private BigDecimal entertainment;
        private BigDecimal leisure_culture;
        private BigDecimal other;
        private BigDecimal eating_out;
    }

    @Getter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class PercentOut {
        private double spending_total;
        private double food;
        private double clothing_footwear;
        private double living_goods;
        private double medical;
        private double transport;
        private double education;
        private double entertainment;
        private double leisure_culture;
        private double other;
        private double eating_out;
    }

    public ReportDescription.DescriptionSummary getDescriptionSummary() { return description_summary; }
    public List<KeywordRow> getKeywords() { return keywords; }
    public PopulationOut getPopulation() { return population; }
    public PriceOut getPrice() { return price; }
    public IncomeConsumptionOut getIncomeConsumption() { return income_consumption; }
    public ReportDescription.DescriptionStrategy getDescriptionStrategy() { return description_strategy; }

    public static TotalReportRes from(ReportData src, ReportDescription desc) {
        return TotalReportRes.builder()
                .description_summary(desc == null ? null : desc.getDescriptionSummary())
                .keywords(toKeywordRows(src.getKeywordReportRes()))
                .population(toPopulationOut(src.getPopulationRes(),
                        desc == null ? null : desc.getDescriptionPopulation()))
                .price(toPriceOut(src.getPriceRes(),
                        desc == null ? null : desc.getDescriptionPrice()))
                .income_consumption(toIncomeConsumptionOut(src.getIncomeConsumptionRes(),
                        desc == null ? null : desc.getIncomeConsumptionDescription()))
                .description_strategy(desc == null ? null : desc.getDescriptionStrategy())
                .build();
    }

    private static List<KeywordRow> toKeywordRows(KeywordReportRes kr) {
        if (kr == null || kr.getKeywords() == null) return List.of();
        List<KeywordRow> out = new ArrayList<>();
        kr.getKeywords().forEach(row -> {
            Platform p = row.getPlatform();
            out.add(KeywordRow.builder()
                    .platform(p == null ? null : p.name())
                    .keywords(row.getKeywords())
                    .descript(row.getDescript())
                    .build());
        });
        return out;
    }

    private static PopulationOut toPopulationOut(PopulationRes p, ReportDescription.DescriptionPopulation d) {
        if (p == null) return null;

        long a10 = getLong(p.getAges() == null ? null : p.getAges().getResident(), 0);
        long a20 = getLong(p.getAges() == null ? null : p.getAges().getResident(), 1);
        long a30 = getLong(p.getAges() == null ? null : p.getAges().getResident(), 2);
        long a40 = getLong(p.getAges() == null ? null : p.getAges().getResident(), 3);
        long a50 = getLong(p.getAges() == null ? null : p.getAges().getResident(), 4);
        long a60 = getLong(p.getAges() == null ? null : p.getAges().getResident(), 5);

        double p10 = getDouble(p.getAges() == null ? null : p.getAges().getPercent(), 0);
        double p20 = getDouble(p.getAges() == null ? null : p.getAges().getPercent(), 1);
        double p30 = getDouble(p.getAges() == null ? null : p.getAges().getPercent(), 2);
        double p40 = getDouble(p.getAges() == null ? null : p.getAges().getPercent(), 3);
        double p50 = getDouble(p.getAges() == null ? null : p.getAges().getPercent(), 4);
        double p60 = getDouble(p.getAges() == null ? null : p.getAges().getPercent(), 5);

        AgesOut ages = AgesOut.builder()
                .resident(AgesResident.builder()
                        .age_10_resident(a10).age_20_resident(a20).age_30_resident(a30)
                        .age_40_resident(a40).age_50_resident(a50).age_60_plus_resident(a60)
                        .build())
                .percent(AgesPercent.builder()
                        .age_10_percent(p10).age_20_percent(p20).age_30_percent(p30)
                        .age_40_percent(p40).age_50_percent(p50).age_60_plus_percent(p60)
                        .build())
                .build();

        long male   = p.getGender() == null ? 0 : p.getGender().getMaleResident();
        long female = p.getGender() == null ? 0 : p.getGender().getFemaleResident();
        double pm   = p.getGender() == null ? 0.0 : p.getGender().getMalePercent();
        double pf   = p.getGender() == null ? 0.0 : p.getGender().getFemalePercent();

        GenderOut gender = GenderOut.builder()
                .resident(GenderResident.builder().male_resident(male).female_resident(female).build())
                .percent(GenderPercent.builder().male_percent(pm).female_percent(pf).build())
                .build();

        return PopulationOut.builder()
                .total_resident(p.getTotalResident())
                .ages(ages)
                .gender(gender)
                .description_population(d)
                .build();
    }

    private static PriceOut toPriceOut(PriceRes pr, ReportDescription.DescriptionPrice dp) {
        if (pr == null) return null;
        PriceBig big = PriceBig.builder()
                .big_average(pr.getBigAverage())
                .big_middle(pr.getBigMedian())
                .big_count(pr.getBigCount())
                .build();
        PriceSmall small = PriceSmall.builder()
                .small_average(pr.getSmallAverage())
                .small_middle(pr.getSmallMedian())
                .small_count(pr.getSmallCount())
                .build();
        return PriceOut.builder()
                .big(big)
                .small(small)
                .price_per_meter(pr.getPricePerMeter())
                .price_per_pyeong(pr.getPricePerPyeong())
                .trading_volume(pr.getTradingVolume())
                .description_price(dp)
                .build();
    }

    private static IncomeConsumptionOut toIncomeConsumptionOut(IncomeConsumptionRes ic, String desc) {
        if (ic == null) return null;

        IncomeOut income = IncomeOut.builder()
                .monthly_income_average(ic.getIncome() == null ? null : ic.getIncome().getMonthlyIncomeAverage())
                .income_class_code(ic.getIncome() == null ? null : ic.getIncome().getIncomeClassCode())
                .build();

        ExpendOut ex = ExpendOut.builder()
                .food(getBD(ic, 0, "food"))
                .clothing_footwear(getBD(ic, 1, "clothing_footwear"))
                .living_goods(getBD(ic, 2, "living_goods"))
                .medical(getBD(ic, 3, "medical"))
                .transport(getBD(ic, 4, "transport"))
                .education(getBD(ic, 5, "education"))
                .entertainment(getBD(ic, 6, "entertainment"))
                .leisure_culture(getBD(ic, 7, "leisure_culture"))
                .other(getBD(ic, 8, "other"))
                .eating_out(getBD(ic, 9, "eating_out"))
                .build();

        PercentOut pc = PercentOut.builder()
                .spending_total(0.0)
                .food(getP(ic, 0, "food"))
                .clothing_footwear(getP(ic, 1, "clothing_footwear"))
                .living_goods(getP(ic, 2, "living_goods"))
                .medical(getP(ic, 3, "medical"))
                .transport(getP(ic, 4, "transport"))
                .education(getP(ic, 5, "education"))
                .entertainment(getP(ic, 6, "entertainment"))
                .leisure_culture(getP(ic, 7, "leisure_culture"))
                .other(getP(ic, 8, "other"))
                .eating_out(getP(ic, 9, "eating_out"))
                .build();

        ConsumptionOut cons = ConsumptionOut.builder()
                .spending_total(ic.getConsumption() == null ? null : ic.getConsumption().getSpendingTotal())
                .expend(ex)
                .percent(pc)
                .build();

        return IncomeConsumptionOut.builder()
                .income(income)
                .consumption(cons)
                .income_consumption_description(desc)
                .build();
    }

    // ---------- util ----------

    private static long getLong(List<Long> xs, int i) {
        if (xs == null || xs.size() <= i || xs.get(i) == null) return 0L;
        return xs.get(i);
    }
    private static double getDouble(List<Double> xs, int i) {
        if (xs == null || xs.size() <= i || xs.get(i) == null) return 0.0;
        return xs.get(i);
    }

    // List<BigDecimal> 또는 Map<String, BigDecimal> 모두 지원
    @SuppressWarnings("unchecked")
    private static BigDecimal getBD(IncomeConsumptionRes ic, int i, String key) {
        if (ic == null || ic.getConsumption() == null) return null;
        Object ex = (Object) ic.getConsumption().getExpend();
        if (ex instanceof List) {
            List<?> xs = (List<?>) ex;
            Object v = (i < xs.size()) ? xs.get(i) : null;
            return v instanceof BigDecimal ? (BigDecimal) v : null;
        }
        if (ex instanceof Map) {
            Map<String, ?> m = (Map<String, ?>) ex;
            Object v = m.get(key);
            return v instanceof BigDecimal ? (BigDecimal) v : null;
        }
        return null;
    }

    // List<Double> 또는 Map<String, Double> 모두 지원
    @SuppressWarnings("unchecked")
    private static double getP(IncomeConsumptionRes ic, int i, String key) {
        if (ic == null || ic.getConsumption() == null) return 0.0;
        Object ex = (Object) ic.getConsumption().getPercent();
        if (ex instanceof List) {
            List<?> xs = (List<?>) ex;
            Object v = (i < xs.size()) ? xs.get(i) : null;
            return v instanceof Double ? (Double) v : 0.0;
        }
        if (ex instanceof Map) {
            Map<String, ?> m = (Map<String, ?>) ex;
            Object v = m.get(key);
            return v instanceof Double ? (Double) v : 0.0;
        }
        return 0.0;
    }
}
