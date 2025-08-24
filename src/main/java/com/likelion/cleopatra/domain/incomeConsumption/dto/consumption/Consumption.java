// src/main/java/com/likelion/cleopatra/domain/incomeConsumption/dto/consumption/Consumption.java
package com.likelion.cleopatra.domain.incomeConsumption.dto.consumption;

import com.likelion.cleopatra.domain.incomeConsumption.document.IncomeConsumptionDoc;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Consumption {
    // 단위: 만원
    private BigDecimal spendingTotal;
    // 단위: 만원 (항목 순서 고정)
    private List<BigDecimal> expend;
    // 단위: %
    private List<Double> percent;

    private static final BigDecimal TEN_THOUSAND = new BigDecimal("10000");

    /** IncomeDoc → Consumption 생성 + 퍼센트 계산(원 기준) + 만원 환산 */
    public static Consumption from(IncomeConsumptionDoc doc) {
        BigDecimal total         = doc.getSpendingTotal();
        BigDecimal food          = doc.getFood();
        BigDecimal clothFoot     = doc.getClothingFootwear();
        BigDecimal living        = doc.getLivingGoods();
        BigDecimal medical       = doc.getMedical();
        BigDecimal transport     = doc.getTransport();
        BigDecimal education     = doc.getEducation();
        BigDecimal entertainment = doc.getEntertainment();
        BigDecimal leisure       = doc.getLeisureCulture();
        BigDecimal other         = doc.getOther();
        BigDecimal eatingOut     = doc.getEatingOut();

        double pFood          = pct(food, total);
        double pClothFoot     = pct(clothFoot, total);
        double pLiving        = pct(living, total);
        double pMedical       = pct(medical, total);
        double pTransport     = pct(transport, total);
        double pEducation     = pct(education, total);
        double pEntertainment = pct(entertainment, total);
        double pLeisure       = pct(leisure, total);
        double pOther         = pct(other, total);
        double pEatingOut     = pct(eatingOut, total);

        return Consumption.builder()
                .spendingTotal(toMw(total))
                .expend(List.of(
                        toMw(food), toMw(clothFoot), toMw(living), toMw(medical),
                        toMw(transport), toMw(education), toMw(entertainment),
                        toMw(leisure), toMw(other), toMw(eatingOut)
                ))
                .percent(List.of(
                        pFood, pClothFoot, pLiving, pMedical, pTransport,
                        pEducation, pEntertainment, pLeisure, pOther, pEatingOut
                ))
                .build();
    }

    private static BigDecimal toMw(BigDecimal v) {
        if (v == null) return BigDecimal.ZERO;
        return v.divide(TEN_THOUSAND, 1, RoundingMode.HALF_UP);
    }

    private static double pct(BigDecimal part, BigDecimal total) {
        if (part == null || total == null || total.signum() == 0) return 0.0;
        double v = part.divide(total, 6, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100)).doubleValue();
        return Math.round(v * 10.0) / 10.0;
    }
}
