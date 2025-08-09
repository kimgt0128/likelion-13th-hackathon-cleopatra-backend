package com.likelion.cleopatra.global.config;

import com.likelion.cleopatra.global.dto.ApiInfo;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import org.springframework.http.HttpHeaders;

/**
 * WebClientConfig
 *
 * - 외부 API 연동(Naver API 등)에서 사용할 WebClient를 설정하는 클래스
 * - @ConfigurationProperties(prefix = "external.api")로 application.yml의 external.api.* 설정값을
 *   ApiInfo DTO로 바인딩하여 사용
 * - 여기서는 네이버 API 전용 WebClient 빈을 등록
 */
@ConfigurationProperties(prefix = "external.api")
@Configuration
public class WebClientConfig {

    private ApiInfo naver = new ApiInfo();

    @Bean(name = "naverWebClient")
    public WebClient naverWebClient() {
        return WebClient.builder()
                .baseUrl(naver.getBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("X-Naver-Client-Id", naver.getClientId())
                .defaultHeader("X-Naver-Client-Secret", naver.getClientSecret())
                .build();
    }
}
