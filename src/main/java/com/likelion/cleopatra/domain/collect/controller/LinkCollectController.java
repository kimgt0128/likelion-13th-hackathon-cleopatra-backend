package com.likelion.cleopatra.domain.collect.controller;

import com.likelion.cleopatra.domain.collect.dto.requeset.CollectNaverLinkReq;
import com.likelion.cleopatra.domain.collect.dto.response.CollectResultRes;
import com.likelion.cleopatra.domain.collect.service.LinkCollectorService;
import com.likelion.cleopatra.global.exception.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Collect", description = "외부 플랫폼 링크 수집 API")
@RestController
@RequestMapping("/api/collect/naver")
@RequiredArgsConstructor
public class LinkCollectController {

    private final LinkCollectorService linkCollectorService;

    @Operation(
            summary = "네이버 블로그 링크 수집",
            description = "행정동 + 2차 카테고리로 네이버 블로그를 검색해 링크를 적재\n" +
                    "예시: 검색 키워드: 공릉동 + 치킨\n"
    )
    @PostMapping("/blog")
    public ApiResponse<CollectResultRes> collectNaverBlog(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CollectNaverLinkReq.class),
                            examples = @ExampleObject(name = "예시",
                                    value = """
                        {
                          "primary": "외식업",
                          "secondary": "카페",
                          "district": "노원구",
                          "neighborhood": "공릉동",
                          "display": (default 50),
                          "start": (default 1)
                        }
                        """
                    )
            )
    )@Valid @RequestBody CollectNaverLinkReq req)
    {
        CollectResultRes res = linkCollectorService.collectNaverBlogLinks(req);
        return ApiResponse.success(res);
    }

    @Operation(
            summary = "네이버 플레이스 링크 수집",
            description = "행정동 + 2차 카테고리로 네이버 플레이스를 검색해 링크를 적재\n예시: 검색 키워드: 공릉동 + 일식"
    )
    @PostMapping("/place")
    public ApiResponse<CollectResultRes> collectNaverPlace(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CollectNaverLinkReq.class),
                            examples = @ExampleObject(name = "예시",
                                    value = """
                        {
                          "primary": "외식업",
                          "secondary": "일식",
                          "district": "노원구",
                          "neighborhood": "공릉동",
                          "display": (default 5)
                        }
                        """
                            )
                    )
            )
            @Valid @RequestBody CollectNaverLinkReq req
    ) {
        //CollectResultRes res = linkCollectorService.collectNaverPlaceLinks(req);
        //return ApiResponse.success(res);
        return null;
    }
}
