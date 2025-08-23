package com.likelion.cleopatra.domain.report.service;

import com.likelion.cleopatra.domain.aiDescription.DescriptionService;
import com.likelion.cleopatra.domain.incomeConsumption.dto.IncomeConsumptionRes;
import com.likelion.cleopatra.domain.incomeConsumption.service.IncomeConsumptionService;
import com.likelion.cleopatra.domain.member.entity.Member;
import com.likelion.cleopatra.domain.member.repository.MemberRepository;
import com.likelion.cleopatra.domain.openApi.rtms.service.RtmsService;
import com.likelion.cleopatra.domain.population.dto.PopulationRes;
import com.likelion.cleopatra.domain.population.service.PopulationService;
import com.likelion.cleopatra.domain.aiDescription.dto.ReportDescriptionRes;
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




    public ReportRes create(String primaryKey, ReportReq req) {
        long t0 = System.nanoTime();

        Member member = memberRepository.findByPrimaryKey(primaryKey)
                        .orElseThrow(null);

        log.debug("[REPORT] create start primaryKey={} district={} neighborhood={} subNeighborhood={} secondary={} anchor=2025-06",
                primaryKey, req.getDistrict(), req.getNeighborhood(), req.getSub_neighborhood(), req.getSecondary());


        PopulationRes populationRes = getPopulation(req);
        PriceRes priceRes = getPrice(req);
        IncomeConsumptionRes incomeConsumptionRes = getIncomeConsumption(req);
        // 근데 이럴거면 차라리 pop, pri, inc을 하나로 묶고, description으로 총 두개의 인자만 넣는게 깔끔하지 않나>
        Report report = Report.create(member, req, populationRes, priceRes, incomeConsumptionRes, 설명dto);

        long ms = (System.nanoTime() - t0) / 1_000_000;
        log.debug("[REPORT] report created in {} ms", ms);
        return ReportRes.from(report);
    }


    /** ----------helper **/

    private PopulationRes getPopulation(ReportReq req) {
        return  populationService.getPopulationData(req);
    }

    private PriceRes getPrice(ReportReq req) {
        String lawdCd = LawdCodeResolver.resolveGuCode5(req.getDistrict());
        String dong   = LawdCodeResolver.pickDongOrThrow(req.getNeighborhood(), req.getSub_neighborhood());

        YearMonth anchor = YearMonth.of(2025, 6); // 기본: 2024-07~2025-06
        return rtmsService.buildPriceRes(lawdCd, anchor, dong);
    }

    private IncomeConsumptionRes getIncomeConsumption(ReportReq req) {
        return incomeConsumptionService.getIncomeConsumptionData(req);
    }

    private ReportDescriptionRes getTotalReportDescription(ReportReq req, PopulationRes populationRes, PriceRes priceRes, IncomeConsumptionRes incomeConsumptionRes) {

        return descriptionService.getDescription(req, populationRes, priceRes, incomeConsumptionRes);

    }
}
