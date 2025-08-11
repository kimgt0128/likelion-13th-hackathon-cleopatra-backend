package com.likelion.cleopatra.domain.data.dto;

import com.likelion.cleopatra.domain.common.enums.addrdss.District;
import com.likelion.cleopatra.domain.common.enums.addrdss.Neighborhood;

public record CollectNaverBlogReq(
        String primary,
        String secondary,
        District district,
        Neighborhood neighborhood,
        Integer display,   // null이면 기본값 적용
        Integer start,     // null이면 기본값 적용
        String query       // null이면 "동 + 카테고리"로 자동 생성
) {}