package com.likelion.cleopatra.domain.collect.dto.requeset;

import com.likelion.cleopatra.global.common.enums.keyword.Primary;
import com.likelion.cleopatra.global.common.enums.keyword.Secondary;
import com.likelion.cleopatra.global.common.enums.address.District;
import com.likelion.cleopatra.global.common.enums.address.Neighborhood;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "CollectNaverLinkReq", description = "네이버 블로그 수집 요청(한글)")
public record CollectNaverLinkReq(

        @NotNull(message = "primary는 필수")
        @Schema(description = "1차 카테고리", example = "외식업", requiredMode = Schema.RequiredMode.REQUIRED)
        Primary primary,

        @NotNull(message = "secondary는 필수")
        @Schema(description = "2차 카테고리", example = "카페", requiredMode = Schema.RequiredMode.REQUIRED)
        Secondary secondary,

        @NotNull(message = "district는 필수")
        @Schema(description = "구", example = "노원구", requiredMode = Schema.RequiredMode.REQUIRED)
        District district,

        @NotNull(message = "neighborhood는 필수")
        @Schema(description = "동", example = "공릉동", requiredMode = Schema.RequiredMode.REQUIRED)
        Neighborhood neighborhood,

        @Schema(description = "한 번에 가져올 개수(최대 50)", example = "10")
        Integer display,

        @Schema(description = "검색 시작 위치(1부터)", example = "1")
        Integer start
) {
    public int displayOrDefault() { return display == null ? 50 : Math.min(50, display); }
    public int startOrDefault()   { return start   == null ? 1  : start; }
}
