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
                .price(mapPrice(rd.getPriceRes()))                  // ★ null 금지/0 채움
                .incomeConsumption(mapIncome(rd.getIncomeConsumptionRes()))
                .build();
    }

    // KeywordReportRes -> data_naver_* 맵 구성
    private Map<String, StrategyReq.PlatformBlock> buildPlatformMap(KeywordReportRes krr) {
        Map<String, StrategyReq.PlatformBlock> out = new LinkedHashMap<>();
        if (krr == null) return out;

        JsonNode root = objectMapper.valueToTree(krr);
        // ★ 실제 응답 배열 필드명은 "keywords"
        JsonNode arr = root.get("keywords"); // ★
        if (arr != null && arr.isArray()) {
            for (JsonNode p : arr) {
                String platform = textOfSingle(p, "platform"); // ★
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

    private String textOfSingle(JsonNode n, String field) { // ★
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

    /** -------- population: 배열/객체 모두 대응 + total 보정 -------- */
    private StrategyReq.Population mapPopulation(PopulationRes src) {
        if (src == null) return null;

        JsonNode root = objectMapper.valueToTree(src);

        Integer totalResident = firstInt(root, "total_resident", "totalResident");

        // ★ total_resident 없거나 0이면 남/녀 합으로 보강
// ★ total_resident 없거나 0이면 남/녀 합으로 보강 (gender.resident.* 에서 읽기)
        if (totalResident == null || totalResident == 0) {
            JsonNode genderNode   = firstNode(root, "gender", "Gender", "GENDER", "GEN");
            JsonNode genResident  = (genderNode == null) ? null
                    : firstNode(genderNode, "resident", "Resident", "RESIDENT");
            Integer male          = firstInt(genResident, "male_resident", "maleResident");
            Integer female        = firstInt(genResident, "female_resident", "femaleResident");
            if (male != null && female != null) totalResident = male + female;
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
                .totalResident(totalResident) // ★ 보정된 값 사용
                .ages(ages)
                .gender(gender)
                .build();
    }

    // ★ PriceRes -> StrategyReq.Price : null 금지, 숫자 없으면 0으로 채움
// ★ PriceRes -> StrategyReq.Price : null 금지, 숫자 없으면 0으로 채움
    private StrategyReq.Price mapPrice(PriceRes src) {
        if (src == null) {
            return StrategyReq.Price.builder()
                    .big(StrategyReq.Price.Big.builder().bigAverage(0).bigMiddle(0).bigCount(0).build())
                    .small(StrategyReq.Price.Small.builder().smallAverage(0).smallMiddle(0).smallCount(0).build())
                    .pricePerMeter(0).pricePerPyeong(0)
                    .tradingVolume(new LinkedHashMap<>(Map.of(
                            "2024_3_quarter", 0,
                            "2024_4_quarter", 0,
                            "2025_1_quarter", 0,
                            "2025_2_quarter", 0
                    )))
                    .build();
        }

        // PriceRes는 평탄한 필드들임에 주의 (bigMedian -> bigMiddle, smallMedian -> smallMiddle 로 매핑)
        StrategyReq.Price.Big big = StrategyReq.Price.Big.builder()
                .bigAverage(nzInt(src.getBigAverage()))
                .bigMiddle (nzInt(src.getBigMedian()))
                .bigCount  (nzInt(src.getBigCount()))
                .build();

        StrategyReq.Price.Small small = StrategyReq.Price.Small.builder()
                .smallAverage(nzInt(src.getSmallAverage()))
                .smallMiddle (nzInt(src.getSmallMedian()))
                .smallCount  (nzInt(src.getSmallCount()))
                .build();

        Map<String, Integer> tv = new LinkedHashMap<>();
        Map<String, Long> raw = src.getTradingVolume();
        tv.put("2024_3_quarter", raw != null && raw.get("2024_3_quarter") != null ? raw.get("2024_3_quarter").intValue() : 0);
        tv.put("2024_4_quarter", raw != null && raw.get("2024_4_quarter") != null ? raw.get("2024_4_quarter").intValue() : 0);
        tv.put("2025_1_quarter", raw != null && raw.get("2025_1_quarter") != null ? raw.get("2025_1_quarter").intValue() : 0);
        tv.put("2025_2_quarter", raw != null && raw.get("2025_2_quarter") != null ? raw.get("2025_2_quarter").intValue() : 0);

        return StrategyReq.Price.builder()
                .big(big)
                .small(small)
                .pricePerMeter (nzInt(src.getPricePerMeter()))
                .pricePerPyeong(nzInt(src.getPricePerPyeong()))
                .tradingVolume (tv)
                .build();
    }

    // IncomeConsumptionRes -> StrategyReq.IncomeConsumption
    private StrategyReq.IncomeConsumption mapIncome(IncomeConsumptionRes src) {
        if (src == null) return null;
        JsonNode r = objectMapper.valueToTree(src);

        JsonNode incomeNode = r.get("income");
        StrategyReq.IncomeConsumption.Income income = StrategyReq.IncomeConsumption.Income.builder()
                .monthlyIncomeAverage(firstInt(incomeNode, "monthly_income_average", "monthlyIncomeAverage"))
                .incomeClassCode    (textOf(incomeNode, "income_class_code", "incomeClassCode"))
                .build();

        StrategyReq.IncomeConsumption.Consumption consumption =
                objectMapper.convertValue(r.get("consumption"), StrategyReq.IncomeConsumption.Consumption.class);

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
        return n == null || n.isMissingNode() || n.isNull()
                ? null
                : (n.isNumber() ? n.intValue() : tryParseInt(n.asText()));
    }

    private Double firstDouble(JsonNode node, String... keys) {
        JsonNode n = firstNode(node, keys);
        return n == null || n.isMissingNode() || n.isNull()
                ? null
                : (n.isNumber() ? n.doubleValue() : tryParseDouble(n.asText()));
    }

    private String textOf(JsonNode n, String... keys) {
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

    private Integer asInt(JsonNode n, String k) {
        if (n == null || !n.has(k) || n.get(k).isNull()) return null;
        JsonNode v = n.get(k);
        if (v.isNumber()) {
            if (v.isInt() || v.isLong()) return v.intValue();
            return (int) Math.round(v.doubleValue());
        }
        return tryParseInt(v.asText());
    }

    private Integer tryParseInt(String s) {
        try { return (int) Math.round(Double.parseDouble(s)); } catch (Exception e) { return null; }
    }

    private Double tryParseDouble(String s) {
        try { return Double.parseDouble(s); } catch (Exception e) { return null; }
    }

    // ★ Number -> int(반올림), null이면 0
    private Integer nzInt(Number n) { // ★
        if (n == null) return 0;
        if (n instanceof Integer i) return i;
        if (n instanceof Long l) return l.intValue();
        return (int) Math.round(n.doubleValue());
    }
}
