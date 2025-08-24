package com.likelion.cleopatra.domain.population.service;

import com.likelion.cleopatra.domain.population.document.PopulationDoc;
import com.likelion.cleopatra.domain.population.dto.PopulationRes;
import com.likelion.cleopatra.domain.population.dto.age.Ages;
import com.likelion.cleopatra.domain.population.dto.gender.Gender;
import com.likelion.cleopatra.domain.population.repository.PopulationRepository;
import com.likelion.cleopatra.domain.report.dto.report.ReportReq;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.Normalizer;

@Slf4j
@RequiredArgsConstructor
@Service
public class PopulationService {

    private final PopulationRepository populationRepository;

    public PopulationRes getPopulationData(ReportReq req) {
        final String raw = req.getSub_neighborhood();
        final String key = normalizeDong(raw); // ==> "공릉1동" 으로 고정

        PopulationDoc doc = populationRepository.findByAdstrdName(key)
                .orElseThrow(() -> new IllegalArgumentException("population not found: " + key));

        return PopulationRes.from(Ages.from(doc), Gender.from(doc));
    }

    /** "공릉 1 동", 전각숫자, NBSP 등을 모두 "공릉1동"으로 정규화 */
    private String normalizeDong(String s) {
        if (s == null) throw new IllegalArgumentException("sub_neighborhood is null");
        // 전각 -> 반각, 조합문자 정규화
        String t = Normalizer.normalize(s, Normalizer.Form.NFKC);
        // NBSP 제거 후 트림
        t = t.replace('\u00A0', ' ').trim();
        // 내부 공백 전부 제거 (공릉 1 동 -> 공릉1동)
        t = t.replaceAll("\\s+", "");
        // 접미사 "동"은 그대로 두되 불필요한 마침표 등 제거 필요시 추가
        return t;
    }
}
