package com.likelion.cleopatra.domain.keywordData.service;

import com.likelion.cleopatra.domain.keywordData.dto.report.KeywordReportRes;
import com.likelion.cleopatra.domain.keywordData.repository.KeywordRepository;
import com.likelion.cleopatra.domain.report.dto.report.ReportReq;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class KeywordService {

    private final KeywordRepository keywordRepository;

    /** 보고서용: 사전 추출된 키워드 로드 → 보고서 DTO로 변환 */
    public KeywordReportRes getExtractedKeyword(ReportReq req) {
        var docs = keywordRepository.findByDistrictAndNeighborhoodAndPrimaryAndSecondary(
                req.getDistrict(), req.getNeighborhood(), req.getPrimary(), req.getSecondary()
        );
        return KeywordReportRes.fromDocs(docs);
    }
}
