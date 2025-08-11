package com.likelion.cleopatra.domain.data.dto.requeset;

import com.likelion.cleopatra.domain.common.enums.address.District;
import com.likelion.cleopatra.domain.common.enums.address.Neighborhood;

public record CollectNaverBlogReq(
        String primary,
        String secondary,
        District district,
        Neighborhood neighborhood,
        Integer display,   // null이면 기본값 적용
        Integer start     // null이면 기본값 적용
) {}