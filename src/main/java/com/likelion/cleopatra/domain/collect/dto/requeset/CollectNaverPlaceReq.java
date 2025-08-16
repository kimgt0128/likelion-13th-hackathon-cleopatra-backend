package com.likelion.cleopatra.domain.collect.dto.requeset;

import com.likelion.cleopatra.global.common.enums.address.District;
import com.likelion.cleopatra.global.common.enums.address.Neighborhood;
import com.likelion.cleopatra.global.common.enums.keyword.Primary;
import com.likelion.cleopatra.global.common.enums.keyword.Secondary;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(name = "CollectNaverPlaceReq", description = "네이버 플레이스 수집 요청(한글)")
public record CollectNaverPlaceReq(
        @NotNull @Schema(description = "1차 카테고리", example = "외식업", requiredMode = Schema.RequiredMode.REQUIRED)
        Primary primary,
        @NotNull @Schema(description = "2차 카테고리", example = "일식", requiredMode = Schema.RequiredMode.REQUIRED)
        Secondary secondary,
        @NotNull @Schema(description = "구", example = "노원구", requiredMode = Schema.RequiredMode.REQUIRED)
        District district,
        @NotNull @Schema(description = "동", example = "공릉동", requiredMode = Schema.RequiredMode.REQUIRED)
        Neighborhood neighborhood,
        @Min(10) @Max(100) @Schema(description = "한 번에 가져올 개수(최대 100)", example = "50")
        Integer display,
        @Min(1) @Max(1000) @Schema(description = "검색 시작 위치(1부터)", example = "1")
        Integer start
) {
    public int displayOrDefault() { return display == null ? 50 : Math.min(100, display); }
    public int startOrDefault()   { return start == null ? 1 : start; }
}