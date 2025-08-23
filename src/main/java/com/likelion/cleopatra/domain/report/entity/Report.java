// src/main/java/com/likelion/cleopatra/domain/report/entity/Report.java
package com.likelion.cleopatra.domain.report.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion.cleopatra.domain.member.entity.Member;
import com.likelion.cleopatra.domain.report.dto.report.ReportReq;
import com.likelion.cleopatra.domain.report.dto.report.TotalReportRes;
import com.likelion.cleopatra.global.common.enums.address.District;
import com.likelion.cleopatra.global.common.enums.address.Neighborhood;
import com.likelion.cleopatra.global.common.enums.keyword.Primary;
import com.likelion.cleopatra.global.common.enums.keyword.Secondary;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 실무 포인트
 * - 조인 없음. 섹션별 JSON 컬럼으로 분할 저장.
 * - dev(H2)에서 json 타입이 깨지면 columnDefinition="TEXT"로 변경.
 * - 추후 인덱싱 필요 시 가상컬럼+인덱스 추가.
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "report")
@Entity
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING) @Column(name="primary_kword",   nullable = false, length = 30)
    private Primary primary;

    @Enumerated(EnumType.STRING) @Column(name="secondary_kword", nullable = false, length = 30)
    private Secondary secondary;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private District district;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private Neighborhood neighborhood;

    @Column(length = 40)
    private String subNeighborhood;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    // 섹션별 JSON
    @Column(columnDefinition = "json", nullable = true)
    private String descriptionSummaryJson;   // $.description_summary

    @Column(columnDefinition = "json", nullable = true)
    private String keywordsJson;             // $.keywords

    @Column(columnDefinition = "json", nullable = true)
    private String populationJson;           // $.population

    @Column(columnDefinition = "json", nullable = true)
    private String priceJson;                // $.price

    @Column(columnDefinition = "json", nullable = true)
    private String incomeConsumptionJson;    // $.income_consumption

    @Column(columnDefinition = "json", nullable = true)
    private String descriptionStrategyJson;  // $.description_strategy

    // 요약 컬럼(선택): 목록 정렬/필터용
    private Long   totalResident;                 // from population
    private Double pricePerPyeong;                // from price
    @Column(precision = 15, scale = 2)
    private BigDecimal monthlyIncomeAvg;          // from income

    /** 팩토리: 데이터 묶음 + 설명 묶음 → 섹션별 JSON 저장 */
    public static Report create(Member member,
                                ReportReq req,
                                TotalReportRes total,
                                ObjectMapper om) {
        return Report.builder()
                .member(member)
                .primary(req.getPrimary())
                .secondary(req.getSecondary())
                .district(req.getDistrict())
                .neighborhood(req.getNeighborhood())
                .subNeighborhood(req.getSub_neighborhood())
                .createdAt(LocalDateTime.now())

                .descriptionSummaryJson(write(om, total.getDescriptionSummary()))
                .keywordsJson(write(om, total.getKeywords()))
                .populationJson(write(om, total.getPopulation()))
                .priceJson(write(om, total.getPrice()))
                .incomeConsumptionJson(write(om, total.getIncomeConsumption()))
                .descriptionStrategyJson(write(om, total.getDescriptionStrategy()))

                .totalResident(safe(() -> total.getPopulation().getTotalResident()))
                .pricePerPyeong(safe(() -> total.getPrice().getPricePerPyeong()))
                .monthlyIncomeAvg(safe(() -> total.getIncomeConsumption().getIncome().getMonthlyIncomeAverage()))
                .build();
    }

    private static String write(ObjectMapper om, Object v) {
        if (v == null) return null;
        try { return om.writeValueAsString(v); }
        catch (Exception e) { throw new IllegalStateException("JSON serialize failed", e); }
    }
    private static <T> T safe(SupplierX<T> s) { try { return s.get(); } catch (Exception e) { return null; } }
    @FunctionalInterface private interface SupplierX<T> { T get() throws Exception; }
}
