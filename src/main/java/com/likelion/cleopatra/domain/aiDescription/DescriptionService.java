// src/main/java/com/likelion/cleopatra/domain/aiDescription/DescriptionService.java
package com.likelion.cleopatra.domain.aiDescription;

import com.likelion.cleopatra.domain.aiDescription.dto.ReportDescription;
import com.likelion.cleopatra.domain.report.dto.report.ReportData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * - WebClient로 AI 서비스 호출해 ReportDescription 수신.
 * - 실패 시 기본 문구로 폴백.
 */

@Service
public class DescriptionService {

    private final WebClient webClient;

    public DescriptionService(@Qualifier("descriptionWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    public ReportDescription getDescription(ReportData data) {
        // TODO: 실제 엔드포인트/요청스키마에 맞게 body 변환
        try {
            return webClient.post()
                    .uri("/strategy")
                    .bodyValue(data) // 필요 시 별도 요청 DTO로 매핑
                    .retrieve()
                    .bodyToMono(ReportDescription.class)
                    .block();
        } catch (Exception e) {
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
