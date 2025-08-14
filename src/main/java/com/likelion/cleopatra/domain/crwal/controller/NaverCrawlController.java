package com.likelion.cleopatra.domain.crwal.controller;


import com.likelion.cleopatra.domain.crwal.dto.CrawlRes;
import com.likelion.cleopatra.domain.crwal.service.NaverCrawlService;
import com.likelion.cleopatra.global.exception.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/crawl/naver/")
@RestController
public class NaverCrawlController {

    private final NaverCrawlService naverCrawlService;

    /**
     * 서버에서 직접 트리거: 플랫폼 NAVER_BLOG 배치 크롤
     * 예) POST /api/crawl/naver/blog?size=50
     */
    @PostMapping("/blog")
    public ApiResponse<CrawlRes> crawlBlog(@RequestParam(defaultValue = "50") int size) {
        return ApiResponse.success(naverCrawlService.naverBlogCrawl(size));
    }

}
