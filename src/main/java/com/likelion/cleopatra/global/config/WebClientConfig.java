package com.likelion.cleopatra.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Configuration
public class WebClientConfig {

    @Value("${service.naver.url}")
    private String naverBaseUrl;
    @Value("${service.naver.client-id}")
    private String naverClientId;
    @Value("${service.naver.client-secret}")
    private String naverClientSecret;

    @Value("${service.rtms.url}")
    private String rtmsBaseUrl;
    @Value("${service.rtms.service-key}")
    private String rtmsSecret;

    @Value("${ai.keyword.base-url}")
    private String aiKeywordBaseUrl;      // e.g. http://ai:8000/api/ai
    @Value("${ai.description.base-url}")
    private String aiDescriptionBaseUrl;  // e.g. http://ai:8000/api/ai

    @Bean(name = "naverWebClient")
    public WebClient naverWebClient() {
        return WebClient.builder()
                .baseUrl(naverBaseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("X-Naver-Client-Id", naverClientId)
                .defaultHeader("X-Naver-Client-Secret", naverClientSecret)
                .build();
    }

    @Bean(name = "rtmsWebClient")
    public WebClient rtmsWebClient() {
        ExchangeFilterFunction addServiceKey = (request, next) -> {
            URI uri = request.url();
            UriComponentsBuilder b = UriComponentsBuilder.fromUri(uri);
            if (!b.build().getQueryParams().containsKey("serviceKey")) {
                b.queryParam("serviceKey", rtmsSecret);
            }
            ClientRequest newReq = ClientRequest.from(request)
                    .url(b.build(true).toUri())
                    .build();
            return next.exchange(newReq);
        };

        return WebClient.builder()
                .baseUrl(rtmsBaseUrl)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_XML_VALUE)
                .filter(addServiceKey)
                .build();
    }

    @Bean(name = "descriptionWebClient")
    public WebClient descriptionWebClient() {
        return WebClient.builder()
                .baseUrl(aiDescriptionBaseUrl)        // http://ai:8000/api/ai
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Bean(name = "keywordWebClient")
    public WebClient keywordWebClient() {
        return WebClient.builder()
                .baseUrl(aiKeywordBaseUrl)            // http://ai:8000/api/ai
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
