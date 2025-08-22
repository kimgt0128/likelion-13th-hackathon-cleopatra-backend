package com.likelion.cleopatra.domain.report.entity;

import com.likelion.cleopatra.domain.member.entity.Member;
import com.likelion.cleopatra.domain.population.dto.PopulationRes;
import com.likelion.cleopatra.domain.report.dto.income.IncomeRes;
import com.likelion.cleopatra.domain.report.dto.price.PriceRes;
import com.likelion.cleopatra.global.common.enums.address.District;
import com.likelion.cleopatra.global.common.enums.address.Neighborhood;
import com.likelion.cleopatra.global.common.enums.keyword.Primary;
import com.likelion.cleopatra.global.common.enums.keyword.Secondary;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor
@Builder
@Table(name = "report")
@Entity
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long id;

    // 작성자(기기 기반 회원)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 검색 축
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private District district;

    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30)
    private Neighborhood neighborhood;

    @Enumerated(EnumType.STRING) @Column(name="primary_kw", nullable = false, length = 30)
    private Primary primary;

    @Enumerated(EnumType.STRING) @Column(name="secondary_kw", nullable = false, length = 30)
    private Secondary secondary;

    // 표지/목록용
    @Column(nullable = false, length = 120)
    private String title;               // 예: "성수동 카페 상권 보고서"
    @Column(nullable = false, length = 100)
    private String areaName;            // 예: "서울 성동구 성수동1가"

    // 인사이트 카드(3세트)
    @ElementCollection
    @CollectionTable(name = "report_keywords1", joinColumns = @JoinColumn(name = "report_id"))
    @Column(name = "keyword", length = 50)
    @OrderColumn(name = "ord")
    @Builder.Default
    private List<String> keyword1 = new ArrayList<>();
    @Column(name = "description1", length = 600)
    private String description1;

    @ElementCollection
    @CollectionTable(name = "report_keywords2", joinColumns = @JoinColumn(name = "report_id"))
    @Column(name = "keyword", length = 50)
    @OrderColumn(name = "ord")
    @Builder.Default
    private List<String> keyword2 = new ArrayList<>();
    @Column(name = "description2", length = 600)
    private String description2;

    @ElementCollection
    @CollectionTable(name = "report_keywords3", joinColumns = @JoinColumn(name = "report_id"))
    @Column(name = "keyword", length = 50)
    @OrderColumn(name = "ord")
    @Builder.Default
    private List<String> keyword3 = new ArrayList<>();
    @Column(name = "description3", length = 600)
    private String description3;

    public static Report create(PopulationRes populationRes, PriceRes priceRes, IncomeRes incomeRes) {
        return Report.builder().build();
    }

    // 지표

}
