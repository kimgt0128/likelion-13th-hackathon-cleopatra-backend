package com.likelion.cleopatra.domain.population.service;

import com.likelion.cleopatra.domain.population.document.PopulationDoc;
import com.likelion.cleopatra.domain.population.dto.PopulationRes;
import com.likelion.cleopatra.domain.population.dto.age.Ages;
import com.likelion.cleopatra.domain.population.dto.description.DescriptionPopulation;
import com.likelion.cleopatra.domain.population.dto.gender.Gender;
import com.likelion.cleopatra.domain.population.repository.PopulationRepository;
import com.likelion.cleopatra.domain.report.dto.ReportReq;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PopulationService {

    private final PopulationRepository populationRepository;

    public PopulationRes getPopulationData(ReportReq req) {

        PopulationDoc doc = populationRepository.findByAdstrdName(req.getSub_neighborhood())
                .orElseThrow(() -> new IllegalArgumentException("population not found: " + req.getSub_neighborhood()));

        Ages ages = Ages.from(doc);
        Gender gender = Gender.from(doc);
        DescriptionPopulation description = null;
        return PopulationRes.from(ages, gender, description);
    }

}
