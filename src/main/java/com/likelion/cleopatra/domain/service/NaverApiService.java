package com.likelion.cleopatra.domain.service;

import com.likelion.cleopatra.domain.dto.blog.NaverBlogSearchRes;
import com.likelion.cleopatra.domain.dto.cafe.NaverCafeSearchRes;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class NaverApiService {

    private final WebClient webClient;

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
    public  Mono<NaverBlogSearchRes> searchBlog(String query, int display, int start) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/search/blog.json")
                        .queryParam("query", query)
                        .queryParam("display", display)
                        .queryParam("start", start)
                        .queryParam("sort", "date")
                        .build())
                .retrieve()
                .bodyToMono(NaverBlogSearchRes.class);
    }

    /**
     * 네이버 카페글 검색 API 비동기 호출
     *
     * @param query   검색어
     * @param display 표시 개수 (최대 100)
     * @param start   시작 위치 (최대 1000)
     */
    public Mono<NaverCafeSearchRes> searchCafe(String query, int display, int start) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/v1/search/cafearticle.json") // 📌 카페글 검색 엔드포인트
                        .queryParam("query", query)
                        .queryParam("display", display)
                        .queryParam("start", start)
                        .queryParam("sort", "date")
                        .build())
                .retrieve()
                .bodyToMono(NaverCafeSearchRes.class);
    }
}
