package com.likelion.cleopatra.domain.incomeConsumption.service;

import com.likelion.cleopatra.domain.incomeConsumption.document.IncomeConsumptionDoc;
import com.likelion.cleopatra.domain.incomeConsumption.dto.IncomeConsumptionRes;
import com.likelion.cleopatra.domain.incomeConsumption.dto.consumption.Consumption;
import com.likelion.cleopatra.domain.incomeConsumption.dto.description.DescriptionIncomeConsumption;
import com.likelion.cleopatra.domain.incomeConsumption.dto.income.Income;
import com.likelion.cleopatra.domain.incomeConsumption.repository.IncomeConsumptionRepository;
import com.likelion.cleopatra.domain.report.dto.report.ReportReq;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class IncomeConsumptionService {

    private final IncomeConsumptionRepository incomeConsumptionRepository;

    public IncomeConsumptionRes getIncomeConsumptionData(ReportReq req) {

        IncomeConsumptionDoc doc = incomeConsumptionRepository.findByAdstrdName(req.getSub_neighborhood())
                .orElseThrow(() -> new IllegalArgumentException("population not found: " + req.getSub_neighborhood()));

        Income income = Income.from(doc);
        Consumption consumption = Consumption.from(doc);
        return IncomeConsumptionRes.from(income, consumption);
    }
}
