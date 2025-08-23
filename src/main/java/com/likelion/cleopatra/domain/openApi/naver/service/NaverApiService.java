package com.likelion.cleopatra.domain.openApi.naver.service;

import com.likelion.cleopatra.domain.openApi.naver.dto.NaverSearchRes;
import com.likelion.cleopatra.domain.openApi.naver.dto.blog.NaverBlogItem;
import com.likelion.cleopatra.domain.openApi.naver.dto.cafe.NaverCafeSearchRes;
import com.likelion.cleopatra.domain.openApi.naver.dto.place.NaverPlaceItem;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class NaverApiService {

    private final WebClient webClient;

    private static final ParameterizedTypeReference<NaverSearchRes<NaverBlogItem>> BLOG_TYPE =
            new ParameterizedTypeReference<>() {};
    private static final ParameterizedTypeReference<NaverSearchRes<NaverPlaceItem>> PLACE_TYPE =
            new ParameterizedTypeReference<>() {};

    // Bean 이름을 명시적으로 구별해주기 위해 생성자 직접 작성
    public NaverApiService(@Qualifier("naverWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * 네이버 블로그 검색 API 비동기 호출
     *
     * @param query 검색어 (예: "공릉 치킨")
     * @return Mono<NaverBlogSearchRes> API 호출 결과를 감싸는 Mono 비동기 스트림
     *
     * URI 예시:
     * https://openapi.naver.com/v1/search/blog?query=공릉 치킨
     *
     * 참고:
     * - .json 확장자는 공식 문서상 생략 권장
     * - 쿼리 파라미터에 띄어쓰기 등 특수문자는 내부에서 자동 인코딩 처리됨
     */
    public  Mono<NaverSearchRes<NaverBlogItem>> searchBlog(String query, int display, int start) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/search/blog.json")
                        .queryParam("query", query)
                        .queryParam("display", display)
                        .queryParam("start", start)
                        .queryParam("sort", "date")
                        .build())
                .retrieve()
                .bodyToMono(BLOG_TYPE);
    }

    /** 지역(플레이스) 검색: Generic 응답 */
    public Mono<NaverSearchRes<NaverPlaceItem>> searchPlace(String query, int display, int start, String sort) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/search/local.json")
                        .queryParam("query", query)
                        .queryParam("display", display) // 문서상 최대 5
                        .queryParam("start", start)     // 문서상 1만 유효
                        .queryParam("sort", (sort == null || sort.isBlank()) ? "random" : sort)
                        .build())
                .retrieve()
                .bodyToMono(PLACE_TYPE);
    }


}
