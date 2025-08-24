package com.likelion.cleopatra.domain.keywordData.dto;

import com.likelion.cleopatra.global.common.enums.address.District;
import com.likelion.cleopatra.global.common.enums.address.Neighborhood;
import com.likelion.cleopatra.global.common.enums.keyword.Primary;
import com.likelion.cleopatra.global.common.enums.keyword.Secondary;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class KeywordExtractReq {
    @Schema(description = "행정 구역", example = "노원구", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private District district;

    @Schema(description = "행정동", example = "공릉동", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Neighborhood neighborhood;

    @Schema(description = "1차 카테고리", example = "외식업", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Primary primary;


    @Schema(description = "2차 카테고리", example = "일식", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull
    private Secondary secondary;
}
