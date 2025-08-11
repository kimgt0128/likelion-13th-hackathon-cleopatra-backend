package com.likelion.cleopatra.global.config;

import com.likelion.cleopatra.global.dto.ApiInfo;
import org.springframework.beans.factory.annotation.Value;
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

    @Value("${service.naver.url}")
    private String naverBaseUrl;
    @Value("${service.naver.client-id}")
    private String naverClientId;
    @Value("${service.naver.client-secret}")
    private String naverClientSecret;

    //private ApiInfo naver = new ApiInfo(naverBaseUrl, naverClientId, naverClientSecret);

    /**
     * 네이버 API 호출용 WebClient 빈 생성
     *
     * @return WebClient
     *
     * 설정 포인트:
     * - baseUrl: 네이버 API 기본 URL (예: https://openapi.naver.com)
     * - Content-Type: JSON 요청/응답을 사용하기 위해 application/json 지정
     * - X-Naver-Client-Id, X-Naver-Client-Secret: 네이버 API 인증 헤더
     *
     * 이렇게 Bean으로 등록하면 서비스 레이어에서 @Qualifier("naverWebClient")로 주입 가능
     * 여러 API(WebClient)를 쓸 때 각기 다른 빈 이름으로 관리하면 확장성 ↑
     */
    @Bean(name = "naverWebClient")
    public WebClient naverWebClient() {
        return WebClient.builder()
                .baseUrl(naverBaseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("X-Naver-Client-Id", naverClientId)
                .defaultHeader("X-Naver-Client-Secret", naverClientSecret)
                .build();
    }
}
