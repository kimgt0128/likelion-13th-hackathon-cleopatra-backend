package com.likelion.cleopatra.domain.report.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion.cleopatra.domain.aiDescription.DescriptionService;
import com.likelion.cleopatra.domain.aiDescription.dto.ReportDescription;
import com.likelion.cleopatra.domain.incomeConsumption.dto.IncomeConsumptionRes;
import com.likelion.cleopatra.domain.incomeConsumption.service.IncomeConsumptionService;
import com.likelion.cleopatra.domain.member.entity.Member;
import com.likelion.cleopatra.domain.member.repository.MemberRepository;
import com.likelion.cleopatra.domain.openApi.rtms.service.RtmsService;
import com.likelion.cleopatra.domain.population.dto.PopulationRes;
import com.likelion.cleopatra.domain.population.service.PopulationService;
import com.likelion.cleopatra.domain.report.dto.keyword.KeywordEntry;
import com.likelion.cleopatra.domain.report.dto.report.TotalReportRes;
import com.likelion.cleopatra.domain.report.dto.report.ReportData;
import com.likelion.cleopatra.domain.report.dto.report.ReportReq;
import com.likelion.cleopatra.domain.report.dto.report.ReportRes;
import com.likelion.cleopatra.domain.report.dto.price.PriceRes;
import com.likelion.cleopatra.domain.report.entity.Report;
import com.likelion.cleopatra.domain.report.repository.ReportRepository;
import com.likelion.cleopatra.global.geo.LawdCodeResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReportService {

    private final MemberRepository memberRepository;
    private final ReportRepository repository;
    private final PopulationService populationService;
    private final RtmsService rtmsService;
    private final IncomeConsumptionService incomeConsumptionService;
    private final DescriptionService descriptionService;
    private final ObjectMapper objectMapper;


    public ReportRes create(String primaryKey, ReportReq req) {
        long t0 = System.nanoTime();

        Member member = memberRepository.findByPrimaryKey(primaryKey)
                .orElseThrow(() -> new IllegalArgumentException("member not found: " + primaryKey));

        log.debug("[REPORT] create start primaryKey={} district={} neighborhood={} subNeighborhood={} secondary={} anchor=2025-06",
                primaryKey, req.getDistrict(), req.getNeighborhood(), req.getSub_neighborhood(), req.getSecondary());



        ReportData reportData = preprocess(req);
        ReportDescription reportDescription = descriptionService.getDescription(reportData);
        TotalReportRes totalReportRes = TotalReportRes.from(reportData, reportDescription);

        Report report = Report.create(member, req, totalReportRes, objectMapper);

        long ms = (System.nanoTime() - t0) / 1_000_000;
        log.debug("[REPORT] report created in {} ms", ms);
        return ReportRes.from(report);
    }


    /** ----------helper **/

    // ReportReq로부터 {PopulationRes, PriceRes, IncomeConsumptionRes}로 가공해주는 함수. 이후에 ai한테 넘겨서 전체 설명을 포함한 TotalReportRes로 만든다.
    private ReportData preprocess(ReportReq req) {

        PopulationRes populationRes = populationService.getPopulationData(req);

        String dong   = LawdCodeResolver.pickDongOrThrow(req.getNeighborhood(), req.getSub_neighborhood());
        YearMonth anchor = YearMonth.of(2025, 6); // 기본: 2024-07~2025-06
        String lawdCd = LawdCodeResolver.resolveGuCode5(req.getDistrict());
        PriceRes priceRes = rtmsService.buildPriceRes(lawdCd, anchor, dong);

        IncomeConsumptionRes incomeConsumptionRes = incomeConsumptionService.getIncomeConsumptionData(req);


        // 키워드는 상황에 따라 채움(없으면 null/빈 리스트) -> 구현 예정
        List<KeywordEntry> keywords = java.util.Collections.emptyList();

        return ReportData.of(populationRes, priceRes, incomeConsumptionRes, keywords);
    }
}
