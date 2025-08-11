package com.likelion.cleopatra.domain.data.controller;

import com.likelion.cleopatra.domain.data.dto.requeset.CollectNaverBlogReq;
import com.likelion.cleopatra.domain.data.dto.response.CollectResultRes;
import com.likelion.cleopatra.domain.data.service.LinkCollectorService;
import com.likelion.cleopatra.global.exception.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/collect")
@RequiredArgsConstructor
public class LinkCollectController {

    private final LinkCollectorService linkCollectorService;

    @PostMapping("/naver/blog")
    public ApiResponse<CollectResultRes> collectNaverBlog(@RequestBody CollectNaverBlogReq req) {
        CollectResultRes res = linkCollectorService.collectNaverBlogLinks(req);
        return ApiResponse.success(res);
    }

}
