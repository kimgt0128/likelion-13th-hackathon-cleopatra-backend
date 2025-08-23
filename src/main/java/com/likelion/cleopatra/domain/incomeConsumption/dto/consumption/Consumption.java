package com.likelion.cleopatra.domain.incomeConsumption.dto.consumption;

import com.likelion.cleopatra.domain.incomeConsumption.document.IncomeConsumptionDoc;
import com.likelion.cleopatra.domain.incomeConsumption.dto.IncomeConsumptionRes;
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
    private BigDecimal spendingTotal;
    private List<BigDecimal> expend;
    private List<Double> percent;

    /** IncomeDoc → Consumption 생성 + 퍼센트 계산 */
    public static Consumption from(IncomeConsumptionDoc doc) {
        BigDecimal total       = nz(doc.getSpendingTotal());
        BigDecimal food        = nz(doc.getFood());
        BigDecimal clothFoot   = nz(doc.getClothingFootwear());
        BigDecimal living      = nz(doc.getLivingGoods());
        BigDecimal medical     = nz(doc.getMedical());
        BigDecimal transport   = nz(doc.getTransport());
        BigDecimal education   = nz(doc.getEducation());
        BigDecimal leisure     = nz(doc.getLeisureCulture());
        BigDecimal other       = nz(doc.getOther());
        BigDecimal eatingOut   = nz(doc.getEatingOut());

        double pFood      = pct(food, total);
        double pClothFoot = pct(clothFoot, total);
        double pLiving    = pct(living, total);
        double pMedical   = pct(medical, total);
        double pTransport = pct(transport, total);
        double pEducation = pct(education, total);
        double pLeisure   = pct(leisure, total);
        double pOther     = pct(other, total);
        double pEatingOut = pct(eatingOut, total);

        return Consumption.builder()
                .spendingTotal(total)
                .expend(List.of(food, clothFoot, living, medical, transport, education, leisure, other, eatingOut))
                .percent(List.of(pFood, pClothFoot, pLiving, pMedical, pTransport, pEducation, pLeisure, pOther, pEatingOut))
                .build();
    }
    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
    private static double pct(BigDecimal part, BigDecimal total) {
        if (total == null || total.signum() == 0) return 0.0;
        double v = part.divide(total, 6, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)).doubleValue();
        return Math.round(v * 10.0) / 10.0; // 소수 1자리
    }
}