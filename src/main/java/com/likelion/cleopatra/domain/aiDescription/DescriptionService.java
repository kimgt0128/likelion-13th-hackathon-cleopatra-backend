// src/main/java/com/likelion/cleopatra/domain/aiDescription/DescriptionService.java
package com.likelion.cleopatra.domain.aiDescription;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion.cleopatra.domain.aiDescription.dto.ReportDescription;
import com.likelion.cleopatra.domain.aiDescription.dto.StrategyReq;
import com.likelion.cleopatra.domain.report.dto.report.ReportData;
import com.likelion.cleopatra.domain.report.dto.report.ReportReq;
import com.likelion.cleopatra.global.exception.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@Service
public class DescriptionService {

    private final WebClient webClient;
    private final ObjectMapper mapper;

    public DescriptionService(
            @Qualifier("descriptionWebClient") WebClient webClient,
            ObjectMapper mapper
    ) {
        this.webClient = webClient;
        this.mapper = mapper;
    }

    public ReportDescription getDescription(StrategyReq req) {
        try {
            log.info("[Report] -> AI /strategy req={}", mapper.writeValueAsString(req));
            ApiResponse<ReportDescription> env = webClient.post()
                    .uri("/strategy")
                    .bodyValue(req)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<ReportDescription>>() {})
                    .block();
            ReportDescription res = env == null ? null : env.getData();
            log.info("[Report] <- AI /strategy res={}", mapper.writeValueAsString(res));
            return res != null ? res : fallback();
        } catch (Exception e) {
            log.info("[Report] AI /strategy fallback cause={}", e.toString());
            return fallback();
        }
    }

    private ReportDescription fallback() {
        return ReportDescription.builder()
                .descriptionSummary(ReportDescription.DescriptionSummary.builder()
                        .totalDescription("데이터 기반 요약 생성 실패. 기본 설명을 제공합니다.")
                        .build())
                .incomeConsumptionDescription("생성 실패. 기본 설명.")
                .build();
    }
}
