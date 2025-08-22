// src/main/java/com/likelion/cleopatra/domain/income/document/IncomeDoc.java
package com.likelion.cleopatra.domain.income.document;

import lombok.*;
import org.springframework.data.annotation.Id;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Document("income")
public class IncomeDoc {
    @Id private String id;

    @Indexed private int period;          // 20251
    @Indexed private String adstrdCode;   // 행정동 코드
    private String adstrdName;

    private BigDecimal monthlyIncomeAvg;
    private String incomeClassCode;

    private BigDecimal spendingTotal;
    private BigDecimal food;
    private BigDecimal clothingFootwear;
    private BigDecimal livingGoods;
    private BigDecimal medical;
    private BigDecimal transport;
    private BigDecimal education;
    private BigDecimal entertainment;
    private BigDecimal leisureCulture;
    private BigDecimal other;
    private BigDecimal eatingOut;
}
