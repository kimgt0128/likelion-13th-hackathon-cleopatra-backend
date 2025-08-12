package com.likelion.cleopatra.domain.data.dto.requeset;

import com.likelion.cleopatra.domain.common.enums.keyword.Primary;
import com.likelion.cleopatra.domain.common.enums.keyword.Secondary;
import com.likelion.cleopatra.domain.common.enums.address.District;
import com.likelion.cleopatra.domain.common.enums.address.Neighborhood;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CollectNaverBlogReq", description = "네이버 블로그 수집 요청(한글)")
public record CollectNaverBlogReq(

        @Schema(description = "1차 카테고리(외식업/서비스업/도매업)", example = "외식업", requiredMode = Schema.RequiredMode.REQUIRED)
        Primary primary,

        @Schema(description = "2차 카테고리(예: 한식/중식/카페 등)", example = "카페", requiredMode = Schema.RequiredMode.REQUIRED)
        Secondary secondary,

        @Schema(description = "구", example = "노원구", requiredMode = Schema.RequiredMode.REQUIRED)
        District district,

        @Schema(description = "동", example = "공릉동", requiredMode = Schema.RequiredMode.REQUIRED)
        Neighborhood neighborhood,

        @Schema(description = "한 번에 가져올 개수(최대 100)", example = "50")
        Integer display,

        @Schema(description = "검색 시작 위치(1부터)", example = "1")
        Integer start
) {}
