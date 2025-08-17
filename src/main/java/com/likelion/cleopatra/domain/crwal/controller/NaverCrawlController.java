package com.likelion.cleopatra.domain.crwal.controller;

import com.likelion.cleopatra.domain.crwal.dto.CrawlRes;
import com.likelion.cleopatra.domain.crwal.service.NaverCrawlService;
import com.likelion.cleopatra.global.exception.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Crawl", description = "네이버 본문/플레이스 리뷰 크롤링 API")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/crawl/naver")
@RestController
public class NaverCrawlController {

    private final NaverCrawlService naverCrawlService;

    @PostMapping("/blog")
    @Operation(
            summary = "네이버 블로그 본문 크롤",
            description = """
            큐(links)에서 네이버 블로그 링크를 가져와 본문을 수집합니다.
            - size: 이번 배치에서 처리할 링크 개수 (기본 10)
            예) POST /api/crawl/naver/blog?size=10
            """
    )
    public ApiResponse<CrawlRes> crawlBlog(
            @Parameter(description = "이번 배치에서 처리할 링크 개수", example = "10")
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.success(naverCrawlService.naverBlogCrawl(size));
    }

    @PostMapping("/place")
    @Operation(
            summary = "네이버 플레이스 방문자 리뷰 크롤",
            description = """
            키워드로 네이버 지도에서 매장을 검색해 상위 'places'개 매장을 선택하고,
            각 매장당 방문자 리뷰를 'per'개씩 수집해 contents 컬렉션에 저장합니다.
            - keyword: 검색 키워드 (예: 공릉 일식)
            - places: 상위 매장 수 (기본 5, 최대 20)
            - per: 매장당 리뷰 수 (기본 10, 최대 20)
            예) POST /api/crawl/naver/place?keyword=공릉%20일식&places=5&per=10
            """
    )
    public ApiResponse<CrawlRes> crawlPlace(
            @Parameter(description = "검색 키워드", example = "공릉 일식")
            @RequestParam String keyword,
            @Parameter(description = "상위 매장 수", example = "5")
            @RequestParam(defaultValue = "5") int places,
            @Parameter(description = "매장당 리뷰 수", example = "10")
            @RequestParam(defaultValue = "10") int per
    ) {
        return ApiResponse.success(naverCrawlService.naverPlaceCrawl(keyword, places, per));
    }
}
