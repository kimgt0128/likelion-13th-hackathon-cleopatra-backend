package com.likelion.cleopatra.domain.report.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion.cleopatra.domain.aiDescription.DescriptionService;
import com.likelion.cleopatra.domain.aiDescription.dto.ReportDescription;
import com.likelion.cleopatra.domain.incomeConsumption.dto.IncomeConsumptionRes;
import com.likelion.cleopatra.domain.incomeConsumption.service.IncomeConsumptionService;
import com.likelion.cleopatra.domain.keywordData.dto.report.KeywordReportRes;
import com.likelion.cleopatra.domain.keywordData.service.KeywordService;
import com.likelion.cleopatra.domain.member.entity.Member;
import com.likelion.cleopatra.domain.member.repository.MemberRepository;
import com.likelion.cleopatra.domain.openApi.rtms.service.RtmsService;
import com.likelion.cleopatra.domain.population.dto.PopulationRes;
import com.likelion.cleopatra.domain.population.service.PopulationService;
import com.likelion.cleopatra.domain.report.dto.ReportDetailRes;
import com.likelion.cleopatra.domain.report.dto.ReportListRes;
import com.likelion.cleopatra.domain.report.dto.report.TotalReportRes;
import com.likelion.cleopatra.domain.report.dto.report.ReportData;
import com.likelion.cleopatra.domain.report.dto.report.ReportReq;
import com.likelion.cleopatra.domain.report.dto.report.ReportRes;
import com.likelion.cleopatra.domain.report.dto.price.PriceRes;
import com.likelion.cleopatra.domain.report.entity.Report;
import com.likelion.cleopatra.domain.report.repository.ReportRepository;
import com.likelion.cleopatra.global.geo.LawdCodeResolver;
import jakarta.transaction.Transactional;
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
    private final ReportRepository reportRepository;
    private final KeywordService keywordsService;
    private final PopulationService populationService;
    private final RtmsService rtmsService;
    private final IncomeConsumptionService incomeConsumptionService;
    private final DescriptionService descriptionService;
    private final ObjectMapper objectMapper;


    @Transactional
    public ReportRes create(String primaryKey, ReportReq req) {

        Member member = memberRepository.findByPrimaryKey(primaryKey)
                .orElseThrow(() -> new IllegalArgumentException("member not found: " + primaryKey));

        ReportData reportData = preprocess(req);
        // 여기서부터 keyword를 포함하여 다시 응답 가져오도록
        ReportDescription reportDescription = descriptionService.getDescription(reportData);
//        이제 데이터로 AI가판단해서 AI 설명을 포함한 진짜 전체 보고서용 데이터를 가져오는지 정합성 판단
//                1. 데이터 추출 형식(노션의 DTO와 비교)
//                2. 데이터 가져올시 논리적 오류가 없는지 확인
//                이대로 GPT한테 물어보기!!
//            해당 로직의 모든 클래스 복사 후 질문하기!(썼던 최근 쓰레드에 그대로입력, 거기가 지금까지 워크플로우 제일 잘 학습되어있음)
        TotalReportRes totalReportRes = TotalReportRes.from(reportData, reportDescription);

        // 이후
        Report report = Report.create(member, req, totalReportRes, objectMapper);

        return ReportRes.from(reportRepository.save(report));
    }

    @Transactional
    public ReportListRes getAll(String primaryKey) {
        Member member = memberRepository.findByPrimaryKey(primaryKey)
                .orElseThrow(() -> new IllegalArgumentException("member not found: " + primaryKey));

        List<Report> reports = reportRepository.findAllByMemberOrderByCreatedAtDesc(member);
        return ReportListRes.from(reports);
    }


    /** ----------helper **/
    // ReportReq로부터 {PopulationRes, PriceRes, IncomeConsumptionRes}로 가공해주는 함수. 이후에 ai한테 넘겨서 전체 설명을 포함한 TotalReportRes로 만든다.
    private ReportData preprocess(ReportReq req) {

        // 키워드는 상황에 따라 채움(없으면 null/빈 리스트) -> 구현 예정
        KeywordReportRes keywordsReportRes = keywordsService.getExtractedKeyword(req);

        PopulationRes populationRes = populationService.getPopulationData(req);

        String dong   = LawdCodeResolver.pickDongOrThrow(req.getNeighborhood(), req.getSub_neighborhood());
        YearMonth anchor = YearMonth.of(2025, 6); // 기본: 2024-07~2025-06
        String lawdCd = LawdCodeResolver.resolveGuCode5(req.getDistrict());
        PriceRes priceRes = rtmsService.buildPriceRes(lawdCd, anchor, dong);

        IncomeConsumptionRes incomeConsumptionRes = incomeConsumptionService.getIncomeConsumptionData(req);

        return ReportData.of(keywordsReportRes, populationRes, priceRes, incomeConsumptionRes);
    }

    public ReportDetailRes get(String primaryKey, Long reportId) {
        Member member = memberRepository.findByPrimaryKey(primaryKey)
                .orElseThrow(() -> new IllegalArgumentException("member not found: " + primaryKey));

        Report report = reportRepository.findByIdAndMember(reportId, member)
                .orElseThrow(() -> new IllegalArgumentException("report not found: " + reportId));

        return ReportDetailRes.from(report, objectMapper);
    }
}
