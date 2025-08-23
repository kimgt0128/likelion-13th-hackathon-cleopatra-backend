package com.likelion.cleopatra.domain.keywordData.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KeywordExtractReq {
    @Schema(description = "행정 구역 + 동", example = "노원구 공릉동", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String area;

    @Schema(description = "검색 키워드", example = "외식업 일식", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank
    private String keyword;
}
