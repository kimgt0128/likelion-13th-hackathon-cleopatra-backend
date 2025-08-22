// src/main/java/com/likelion/cleopatra/domain/report/service/ReportService.java
package com.likelion.cleopatra.domain.report.service;

import com.likelion.cleopatra.domain.openApi.rtms.service.RtmsService;
import com.likelion.cleopatra.domain.population.dto.PopulationRes;
import com.likelion.cleopatra.domain.population.service.PopulationService;
import com.likelion.cleopatra.domain.report.dto.ReportReq;
import com.likelion.cleopatra.domain.report.dto.ReportRes;
import com.likelion.cleopatra.domain.report.dto.income.IncomeRes;
import com.likelion.cleopatra.domain.report.dto.price.PriceRes;
import com.likelion.cleopatra.domain.report.entity.Report;
import com.likelion.cleopatra.domain.report.repository.ReportRepository;
import com.likelion.cleopatra.global.geo.LawdCodeResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.YearMonth;

@RequiredArgsConstructor
@Service
public class ReportService {

    private final ReportRepository repository;
    private final PopulationService populationService;
    private final RtmsService rtmsService;




    public ReportRes create(ReportReq req) {
        PopulationRes populationRes = getPopulationRes(req);
        PriceRes priceRes = getPriceRes(req);
        IncomeRes incomeRes = getIncomeRes(req);
        Report report = Report.create(populationRes, priceRes, incomeRes);
        return ReportRes.from(report);

    }



    private PopulationRes getPopulationRes(ReportReq req) {
        return  populationService.getPopulationData(req);
    }

    private PriceRes getPriceRes(ReportReq req) {
        String lawdCd = LawdCodeResolver.resolveGuCode5(req.getDistrict());
        String dong   = LawdCodeResolver.pickDongOrThrow(req.getNeighborhood(), req.getSub_neighborhood());

        YearMonth anchor = YearMonth.of(2025, 6); // 기본: 2024-07~2025-06
        return rtmsService.buildPriceRes(lawdCd, anchor, dong);
    }

    private IncomeRes getIncomeRes(ReportReq req) {
        return null;
    }
}
