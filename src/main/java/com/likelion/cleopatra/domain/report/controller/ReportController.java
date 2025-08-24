package com.likelion.cleopatra.domain.report.controller;

import com.likelion.cleopatra.domain.report.ReportListRes;
import com.likelion.cleopatra.domain.report.dto.report.ReportReq;
import com.likelion.cleopatra.domain.report.dto.report.ReportRes;
import com.likelion.cleopatra.domain.report.service.ReportService;
import com.likelion.cleopatra.global.exception.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Report", description = "상권 분석 보고서 API")
@RequiredArgsConstructor
@RequestMapping("/api/report")
@RestController
@Validated
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/{primary_key}")
    @Operation(
            summary = "보고서 생성",
            description = "경로 변수로 primary_key를 받고, 요청 바디로 필터를 받아 보고서를 생성합니다.\n" +
                    "예) POST /api/report/{primary_key}"
    )
    public ApiResponse<ReportRes> create(
            @Parameter(description = "멤버 식별 키(snake_case 경로 변수명)", example = "1111")
            @PathVariable("primary_key") String primaryKey,
            @RequestBody ReportReq req
    ) {
        return ApiResponse.success(reportService.create(primaryKey, req));
    }

    @GetMapping("/{primary_key}")
    @Operation(
            summary = "리포트 목록 조회",
            description = "멤버의 리포트 목록을 생성일 내림차순으로 조회합니다.\n예) GET /api/report/{primary_key}"
    )
    public ApiResponse<ReportListRes> list(
            @Parameter(description = "멤버 식별 키(snake_case 경로 변수명)", example = "1111")
            @PathVariable("primary_key") String primaryKey
    ) {
        return ApiResponse.success(reportService.getAll(primaryKey));
    }
}
