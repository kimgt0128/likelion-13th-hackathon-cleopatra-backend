package com.likelion.cleopatra.domain.data.controller;

import com.likelion.cleopatra.domain.data.dto.CollectNaverBlogReq;
import com.likelion.cleopatra.domain.data.service.LinkCollectorService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/collect")
@RequiredArgsConstructor
public class LinkCollectController {

    private final LinkCollectorService service;

    @PostMapping("/naver/blog")
    public Map<String, Object> collectNaverBlog(@RequestBody CollectNaverBlogReq req) {
        int inserted = service.collectNaverBlogLinks(req);
        return Map.of("inserted", inserted);
    }
}
