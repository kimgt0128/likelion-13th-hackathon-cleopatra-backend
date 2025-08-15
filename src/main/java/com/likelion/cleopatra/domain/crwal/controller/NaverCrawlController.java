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

@Tag(name = "Crawl", description = "네이버 본문 크롤링 API")
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/crawl/naver/")
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
            @RequestParam(defaultValue = "10") int size)
    {
        return ApiResponse.success(naverCrawlService.naverBlogCrawl(size));
    }

}
