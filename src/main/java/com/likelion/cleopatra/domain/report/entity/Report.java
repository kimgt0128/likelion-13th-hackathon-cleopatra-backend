// src/main/java/com/likelion/cleopatra/domain/report/entity/Report.java
package com.likelion.cleopatra.domain.report.entity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion.cleopatra.domain.member.entity.Member;
import com.likelion.cleopatra.domain.report.dto.price.PriceRes;
import com.likelion.cleopatra.domain.population.dto.PopulationRes;
import com.likelion.cleopatra.domain.incomeConsumption.dto.IncomeConsumptionRes;
import com.likelion.cleopatra.global.common.enums.address.District;
import com.likelion.cleopatra.global.common.enums.address.Neighborhood;
import com.likelion.cleopatra.global.common.enums.keyword.Primary;
import com.likelion.cleopatra.global.common.enums.keyword.Secondary;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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

    /** 섹션별 JSON 컬럼 (MySQL 8: JSON / H2 dev: TEXT 권장) */
    @Column(columnDefinition = "json", nullable = false)
    private String descriptionSummaryJson;

    @Column(columnDefinition = "json", nullable = false)
    private String keywordsJson;

    @Column(columnDefinition = "json", nullable = false)
    private String populationJson;

    @Column(columnDefinition = "json", nullable = false)
    private String priceJson;

    @Column(columnDefinition = "json", nullable = false)
    private String incomeConsumptionJson;

    @Column(columnDefinition = "json", nullable = false)
    private String descriptionStrategyJson;

    /** 섹션 객체 → JSON 직렬화 후 분할 저장 */
    public static Report create(Member member,
                                Primary primary, Secondary secondary,
                                District district, Neighborhood neighborhood, String subNeighborhood,
                                Object descriptionSummary,      // Map/DTO 가능
                                Object keywords,                // List<KeywordEntry> 등
                                PopulationRes population,
                                PriceRes price,
                                IncomeConsumptionRes incomeConsumption,
                                Object descriptionStrategy,     // Map/DTO 가능
                                ObjectMapper om) {

        return Report.builder()
                .member(member)
                .primary(primary)
                .secondary(secondary)
                .district(district)
                .neighborhood(neighborhood)
                .subNeighborhood(subNeighborhood)
                .createdAt(LocalDateTime.now())
                .descriptionSummaryJson(write(om, descriptionSummary))
                .keywordsJson(write(om, keywords))
                .populationJson(write(om, population))
                .priceJson(write(om, price))
                .incomeConsumptionJson(write(om, incomeConsumption))
                .descriptionStrategyJson(write(om, descriptionStrategy))
                .build();
    }

    private static String write(ObjectMapper om, Object v) {
        try { return om.writeValueAsString(v); }
        catch (Exception e) { throw new IllegalStateException("JSON serialize failed", e); }
    }
    private static <T> T safe(SupplierX<T> s) {
        try { return s.get(); } catch (Exception e) { return null; }
    }
    @FunctionalInterface private interface SupplierX<T> { T get() throws Exception; }
}
