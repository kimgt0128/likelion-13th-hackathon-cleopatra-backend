package com.likelion.cleopatra.domain.keywordData.controller;


import com.likelion.cleopatra.domain.keywordData.dto.KeywordExtractReq;
import com.likelion.cleopatra.domain.keywordData.dto.KeywordExtractRes;
import com.likelion.cleopatra.domain.keywordData.service.KeywordService;
import com.likelion.cleopatra.global.exception.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Keyword", description = "사전 키워드 추출 API")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/keyword")
public class KeywordController {

    private final KeywordService keywordService;

    @PostMapping("/extract")
    @Operation(
            summary = "키워드 추출 및 저장",
            description = "크롤링된 블로그/플레이스/유튜브 본문을 AI에 전달해 플랫폼별 키워드와 해설을 추출하고 MongoDB에 저장합니다."
    )
    public ApiResponse<KeywordExtractRes> extract(@Valid @RequestBody KeywordExtractReq req) {
        KeywordExtractRes res = keywordService.analyzeAndSave(req.getArea(), req.getKeyword());
        return ApiResponse.success(res);
    }
}