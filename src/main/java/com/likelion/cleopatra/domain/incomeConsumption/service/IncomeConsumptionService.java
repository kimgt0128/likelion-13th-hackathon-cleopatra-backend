package com.likelion.cleopatra.domain.incomeConsumption.service;

import com.likelion.cleopatra.domain.incomeConsumption.document.IncomeConsumptionDoc;
import com.likelion.cleopatra.domain.incomeConsumption.dto.IncomeConsumptionRes;
import com.likelion.cleopatra.domain.incomeConsumption.dto.consumption.Consumption;
import com.likelion.cleopatra.domain.incomeConsumption.dto.income.Income;
import com.likelion.cleopatra.domain.incomeConsumption.repository.IncomeConsumptionRepository;
import com.likelion.cleopatra.domain.report.dto.report.ReportReq;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.Normalizer;

@RequiredArgsConstructor
@Service
public class IncomeConsumptionService {

    private final IncomeConsumptionRepository incomeConsumptionRepository;

    public IncomeConsumptionRes getIncomeConsumptionData(ReportReq req) {

        String key = normalizeDong(req.getSub_neighborhood());

        IncomeConsumptionDoc doc = incomeConsumptionRepository.findByAdstrdName(key)
                .orElseThrow(() -> new IllegalArgumentException("income/consumption not found: " + key));

        Income income = Income.from(doc);
        Consumption consumption = Consumption.from(doc);
        return IncomeConsumptionRes.from(income, consumption);
    }

    /** "공릉 1동", 전각숫자, NBSP 등 → "공릉1동" 으로 정규화 */
    private String normalizeDong(String s) {
        if (s == null) throw new IllegalArgumentException("sub_neighborhood is null");
        String t = Normalizer.normalize(s, Normalizer.Form.NFKC);
        t = t.replace('\u00A0', ' ').trim();
        return t.replaceAll("\\s+", "");
    }
}
