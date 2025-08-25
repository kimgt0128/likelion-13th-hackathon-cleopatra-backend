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
import com.likelion.cleopatra.domain.report.dto.price.PriceRes;
import com.likelion.cleopatra.domain.report.dto.report.ReportData;
import com.likelion.cleopatra.domain.report.dto.report.ReportReq;
import com.likelion.cleopatra.domain.report.dto.report.ReportRes;
import com.likelion.cleopatra.domain.report.dto.report.TotalReportRes;
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

        StrategyReq strategyReq = toStrategyReq(req, reportData);
        ReportDescription reportDescription = descriptionService.getDescription(strategyReq);

        TotalReportRes totalReportRes = TotalReportRes.from(reportData, reportDescription);

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

    /** ---------- helper ---------- **/
    private ReportData preprocess(ReportReq req) {
        KeywordReportRes keywordsReportRes = keywordsService.getExtractedKeyword(req);

        PopulationRes populationRes = populationService.getPopulationData(req);

        String dong = LawdCodeResolver.pickDongOrThrow(req.getNeighborhood(), req.getSub_neighborhood());
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

        JsonNode root = objectMapper.valueToTree(krr);
        // ★ "platforms"가 아니라 실제 응답 필드 "keywords" 에 배열이 들어있음
        JsonNode arr = root.get("keywords"); // ★ 변경
        if (arr != null && arr.isArray()) {
            for (JsonNode p : arr) {
                String platform = textOfSingle(p, "platform"); // ★ 메서드명 변경(오버로드 구분)
                List<String> keywords = toStringList(p.get("keywords"));
                String descript = textOfSingle(p, "descript");

                String key = platformKey(platform);
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
            case "NAVER_PLACE": return "data_naver_place";
            case "YOUTUBE":     return "data_youtube";
            default:            return null;
        }
    }

    private String textOfSingle(JsonNode n, String field) { // ★ 기존 textOf(String) 혼동 방지
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

    /** -------- population: 배열/객체 모두 대응 -------- */
    private StrategyReq.Population mapPopulation(PopulationRes src) {
        if (src == null) return null;

        JsonNode root = objectMapper.valueToTree(src);

        // total_resident / totalResident
        Integer totalResident = firstInt(root, "total_resident", "totalResident");

        // ★ total_resident 없거나 0이면 남/녀 합으로 보강
        if (totalResident == null || totalResident == 0) { // ★ 추가
            Integer male   = firstInt(firstNode(root, "gender", "Gender", "GENDER", "GEN"), "male_resident", "maleResident");   // ★ 추가
            Integer female = firstInt(firstNode(root, "gender", "Gender", "GENDER", "GEN"), "female_resident", "femaleResident"); // ★ 추가
            if (male != null && female != null) totalResident = male + female; // ★ 추가
        }

        // ages
        JsonNode agesNode = firstNode(root, "ages", "age");
        StrategyReq.Population.Ages.Resident residentAges;
        StrategyReq.Population.Ages.Percent percentAges;

        JsonNode agesResident = agesNode != null ? agesNode.get("resident") : null;
        if (agesResident != null && agesResident.isArray()) {
            residentAges = StrategyReq.Population.Ages.Resident.builder()
                    .age10Resident(intAt(agesResident, 0))
                    .age20Resident(intAt(agesResident, 1))
                    .age30Resident(intAt(agesResident, 2))
                    .age40Resident(intAt(agesResident, 3))
                    .age50Resident(intAt(agesResident, 4))
                    .age60PlusResident(intAt(agesResident, 5))
                    .build();
        } else {
            JsonNode r = agesResident == null ? objectMapper.createObjectNode() : agesResident;
            residentAges = StrategyReq.Population.Ages.Resident.builder()
                    .age10Resident(firstInt(r, "age_10_resident", "age10Resident"))
                    .age20Resident(firstInt(r, "age_20_resident", "age20Resident"))
                    .age30Resident(firstInt(r, "age_30_resident", "age30Resident"))
                    .age40Resident(firstInt(r, "age_40_resident", "age40Resident"))
                    .age50Resident(firstInt(r, "age_50_resident", "age50Resident"))
                    .age60PlusResident(firstInt(r, "age_60_plus_resident", "age60PlusResident"))
                    .build();
        }

        JsonNode agesPercent = agesNode != null ? agesNode.get("percent") : null;
        if (agesPercent != null && agesPercent.isArray()) {
            percentAges = StrategyReq.Population.Ages.Percent.builder()
                    .age10Percent(doubleAt(agesPercent, 0))
                    .age20Percent(doubleAt(agesPercent, 1))
                    .age30Percent(doubleAt(agesPercent, 2))
                    .age40Percent(doubleAt(agesPercent, 3))
                    .age50Percent(doubleAt(agesPercent, 4))
                    .age60PlusPercent(doubleAt(agesPercent, 5))
                    .build();
        } else {
            JsonNode p = agesPercent == null ? objectMapper.createObjectNode() : agesPercent;
            percentAges = StrategyReq.Population.Ages.Percent.builder()
                    .age10Percent(firstDouble(p, "age_10_percent", "age10Percent"))
                    .age20Percent(firstDouble(p, "age_20_percent", "age20Percent"))
                    .age30Percent(firstDouble(p, "age_30_percent", "age30Percent"))
                    .age40Percent(firstDouble(p, "age_40_percent", "age40Percent"))
                    .age50Percent(firstDouble(p, "age_50_percent", "age50Percent"))
                    .age60PlusPercent(firstDouble(p, "age_60_plus_percent", "age60PlusPercent"))
                    .build();
        }

        StrategyReq.Population.Ages ages = StrategyReq.Population.Ages.builder()
                .resident(residentAges)
                .percent(percentAges)
                .build();

        // gender
        JsonNode genderNode = firstNode(root, "gender");
        StrategyReq.Population.Gender.Resident residentGender;
        StrategyReq.Population.Gender.Percent percentGender;

        JsonNode genderResident = genderNode != null ? genderNode.get("resident") : null;
        if (genderResident != null && genderResident.isArray()) {
            residentGender = StrategyReq.Population.Gender.Resident.builder()
                    .maleResident(intAt(genderResident, 0))
                    .femaleResident(intAt(genderResident, 1))
                    .build();
        } else {
            JsonNode gr = genderResident == null ? objectMapper.createObjectNode() : genderResident;
            residentGender = StrategyReq.Population.Gender.Resident.builder()
                    .maleResident(firstInt(gr, "male_resident", "maleResident"))
                    .femaleResident(firstInt(gr, "female_resident", "femaleResident"))
                    .build();
        }

        JsonNode genderPercent = genderNode != null ? genderNode.get("percent") : null;
        if (genderPercent != null && genderPercent.isArray()) {
            percentGender = StrategyReq.Population.Gender.Percent.builder()
                    .malePercent(doubleAt(genderPercent, 0))
                    .femalePercent(doubleAt(genderPercent, 1))
                    .build();
        } else {
            JsonNode gp = genderPercent == null ? objectMapper.createObjectNode() : genderPercent;
            percentGender = StrategyReq.Population.Gender.Percent.builder()
                    .malePercent(firstDouble(gp, "male_percent", "malePercent"))
                    .femalePercent(firstDouble(gp, "female_percent", "femalePercent"))
                    .build();
        }

        StrategyReq.Population.Gender gender = StrategyReq.Population.Gender.builder()
                .resident(residentGender)
                .percent(percentGender)
                .build();

        return StrategyReq.Population.builder()
                .totalResident(totalResident)
                .ages(ages)
                .gender(gender)
                .build();
    }

    // PriceRes -> StrategyReq.Price (명시 매핑)
    private StrategyReq.Price mapPrice(PriceRes src) {
        if (src == null) return null;
        JsonNode r = objectMapper.valueToTree(src); // ★ 변경: convertValue 제거, 수동 매핑

        JsonNode big = r.get("big");
        StrategyReq.Price.Big bigDto = big == null ? null : StrategyReq.Price.Big.builder()
                .bigAverage(asInt(big, "big_average"))   // ★ 추가
                .bigMiddle (asInt(big, "big_middle"))    // ★ 추가
                .bigCount  (asInt(big, "big_count"))     // ★ 추가
                .build();

        JsonNode small = r.get("small");
        StrategyReq.Price.Small smallDto = small == null ? null : StrategyReq.Price.Small.builder()
                .smallAverage(asInt(small, "small_average")) // ★ 추가
                .smallMiddle (asInt(small, "small_middle"))  // ★ 추가
                .smallCount  (asInt(small, "small_count"))   // ★ 추가
                .build();

        Map<String, Integer> tv = null;
        if (r.has("trading_volume") && !r.get("trading_volume").isNull()) {
            tv = objectMapper.convertValue(r.get("trading_volume"), Map.class); // ★ 추가
        }

        return StrategyReq.Price.builder()
                .big(bigDto)
                .small(smallDto)
                .pricePerMeter (asInt(r, "price_per_meter"))  // ★ 추가
                .pricePerPyeong(asInt(r, "price_per_pyeong")) // ★ 추가
                .tradingVolume (tv)
                .build();
    }

    // IncomeConsumptionRes -> StrategyReq.IncomeConsumption (소득 보강)
    private StrategyReq.IncomeConsumption mapIncome(IncomeConsumptionRes src) {
        if (src == null) return null;
        JsonNode r = objectMapper.valueToTree(src); // ★ 변경: 일부 명시 매핑

        JsonNode incomeNode = r.get("income");
        StrategyReq.IncomeConsumption.Income income = StrategyReq.IncomeConsumption.Income.builder()
                .monthlyIncomeAverage(firstInt(incomeNode, "monthly_income_average", "monthlyIncomeAverage")) // ★ 추가
                .incomeClassCode    (textOf(incomeNode, "income_class_code", "incomeClassCode"))             // ★ 추가
                .build();

        StrategyReq.IncomeConsumption.Consumption consumption =
                objectMapper.convertValue(r.get("consumption"), StrategyReq.IncomeConsumption.Consumption.class); // ★ 유지

        return StrategyReq.IncomeConsumption.builder()
                .income(income)
                .consumption(consumption)
                .build();
    }

    /** ------ JSON helpers ------ */
    private int intAt(JsonNode arr, int i) {
        if (arr == null || !arr.isArray() || i >= arr.size() || i < 0) return 0;
        JsonNode n = arr.get(i);
        return n == null || n.isNull() ? 0 : n.asInt(0);
    }

    private double doubleAt(JsonNode arr, int i) {
        if (arr == null || !arr.isArray() || i >= arr.size() || i < 0) return 0.0;
        JsonNode n = arr.get(i);
        return n == null || n.isNull() ? 0.0 : n.asDouble(0.0);
    }

    private Integer firstInt(JsonNode node, String... keys) {
        JsonNode n = firstNode(node, keys);
        return n == null || n.isMissingNode() || n.isNull() ? null : (n.isNumber() ? n.intValue() : tryParseInt(n.asText())); // ★ 약간 보강
    }

    private Double firstDouble(JsonNode node, String... keys) {
        JsonNode n = firstNode(node, keys);
        return n == null || n.isMissingNode() || n.isNull() ? null : (n.isNumber() ? n.doubleValue() : tryParseDouble(n.asText())); // ★ 보강
    }

    private String textOf(JsonNode n, String... keys) { // ★ 추가(가변 인자)
        JsonNode v = firstNode(n, keys);
        return (v == null || v.isNull()) ? null : v.asText();
    }

    private JsonNode firstNode(JsonNode node, String... keys) {
        if (node == null || keys == null) return null;
        for (String k : keys) {
            if (node.has(k)) return node.get(k);
        }
        return null;
    }

    private Integer asInt(JsonNode n, String k) { // ★ 추가 (double->int 반올림 대응)
        if (n == null || !n.has(k) || n.get(k).isNull()) return null;
        JsonNode v = n.get(k);
        if (v.isNumber()) {
            if (v.isInt() || v.isLong()) return v.intValue();
            return (int) Math.round(v.doubleValue());
        }
        return tryParseInt(v.asText());
    }

    private Integer tryParseInt(String s) { // ★ 추가
        try { return (int) Math.round(Double.parseDouble(s)); } catch (Exception e) { return null; }
    }

    private Double tryParseDouble(String s) { // ★ 추가
        try { return Double.parseDouble(s); } catch (Exception e) { return null; }
    }
}
