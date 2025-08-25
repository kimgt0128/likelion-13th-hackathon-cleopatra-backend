package com.likelion.cleopatra.domain.report.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion.cleopatra.domain.aiDescription.DescriptionService;
import com.likelion.cleopatra.domain.aiDescription.dto.ReportDescription;
import com.likelion.cleopatra.domain.aiDescription.dto.StrategyReq;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

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


        // ReportData -> AI 계약 스키마(StrategyReq)로 변환
        StrategyReq strategyReq = toStrategyReq(req, reportData);
        // AI 설명 수신
        ReportDescription reportDescription = descriptionService.getDescription(strategyReq);

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

    public ReportDetailRes get(String primaryKey, Long reportId) {
        Member member = memberRepository.findByPrimaryKey(primaryKey)
                .orElseThrow(() -> new IllegalArgumentException("member not found: " + primaryKey));

        Report report = reportRepository.findByIdAndMember(reportId, member)
                .orElseThrow(() -> new IllegalArgumentException("report not found: " + reportId));

        return ReportDetailRes.from(report, objectMapper);
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

    private StrategyReq toStrategyReq(ReportReq req, ReportData rd) {
        return StrategyReq.builder()
                .area(req.getDistrict().getKo() + " " + req.getNeighborhood().getKo())
                .category(req.getPrimary().getKo() + " " + req.getSecondary().getKo())
                .data(buildPlatformMap(rd.getKeywordReportRes()))
                .population(mapPopulation(rd.getPopulationRes()))
                .price(mapPrice(rd.getPriceRes()))
                .incomeConsumption(mapIncome(rd.getIncomeConsumptionRes()))
                .build();
    }

    // KeywordReportRes -> data_naver_* 맵 구성
    private Map<String, StrategyReq.PlatformBlock> buildPlatformMap(KeywordReportRes krr) {
        Map<String, StrategyReq.PlatformBlock> out = new LinkedHashMap<>();
        if (krr == null) return out;

        // 플랫폼 리스트를 안전하게 파싱(JsonNode 사용) — 실제 필드명: platforms[].{platform, keywords[], descript}
        JsonNode root = objectMapper.valueToTree(krr);
        JsonNode platforms = root.get("platforms");
        if (platforms != null && platforms.isArray()) {
            for (JsonNode p : platforms) {
                String platform = textOf(p, "platform");
                List<String> keywords = toStringList(p.get("keywords"));
                String descript = textOf(p, "descript");

                String key = platformKey(platform); // data_naver_place 철자 주의
                if (key != null) {
                    out.put(key, StrategyReq.PlatformBlock.builder()
                            .platform(platform)
                            .platformKeyword(keywords)
                            .platformDescription(descript)
                            .build());
                }
            }
        }
        return out;
    }

    private String platformKey(String platform) {
        if (platform == null) return null;
        String s = platform.trim().toUpperCase(Locale.ROOT);
        switch (s) {
            case "NAVER_BLOG":  return "data_naver_blog";
            case "NAVER_PLACE": return "data_naver_place"; // ← place
            case "YOUTUBE":     return "data_youtube";
            default:            return null;
        }
    }

    private String textOf(JsonNode n, String field) {
        JsonNode v = n == null ? null : n.get(field);
        return v != null && !v.isNull() ? v.asText() : null;
    }

    private List<String> toStringList(JsonNode arr) {
        if (arr == null || !arr.isArray()) return List.of();
        return StreamSupport.stream(arr.spliterator(), false)
                .filter(x -> x != null && !x.isNull())
                .map(JsonNode::asText)
                .collect(Collectors.toList());
    }

    // PopulationRes -> StrategyReq.Population
    private StrategyReq.Population mapPopulation(PopulationRes src) {
        if (src == null) return null;
        return objectMapper.convertValue(src, StrategyReq.Population.class);
    }

    // PriceRes -> StrategyReq.Price
    private StrategyReq.Price mapPrice(PriceRes src) {
        if (src == null) return null;
        return objectMapper.convertValue(src, StrategyReq.Price.class);
    }

    // IncomeConsumptionRes -> StrategyReq.IncomeConsumption
    private StrategyReq.IncomeConsumption mapIncome(IncomeConsumptionRes src) {
        if (src == null) return null;
        return objectMapper.convertValue(src, StrategyReq.IncomeConsumption.class);
    }

}
