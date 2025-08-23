package com.likelion.cleopatra.domain.aiDescription;

import com.likelion.cleopatra.domain.incomeConsumption.dto.IncomeConsumptionRes;
import com.likelion.cleopatra.domain.population.dto.PopulationRes;
import com.likelion.cleopatra.domain.aiDescription.dto.ReportDescriptionRes;
import com.likelion.cleopatra.domain.report.dto.price.PriceRes;
import com.likelion.cleopatra.domain.report.dto.report.ReportReq;
import org.springframework.stereotype.Service;

@Service
public class DescriptionService {

    // webClientConfig에서 가져오기 설정

    public ReportDescriptionRes getDescription(ReportReq req, PopulationRes populationRes, PriceRes priceRes, IncomeConsumptionRes incomeConsumptionRes) {
        return webClient.get()
                //이 설명은 ai가 주는거라 받아 쓰면될듯
    }
}
