package com.likelion.cleopatra.global.common.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "Platform",
        description = "수집 대상 외부 플랫폼",
        example = "NAVER_BLOG",
        allowableValues = {
                "NAVER_BLOG",
                "NAVER_CAFE",
                "NAVER_PLACE",
                "KAKAO",
                "INSTAGRAM",
                "BAEMIN",
                "YOUTUBE"
        }
)
public enum Platform {
    NAVER_BLOG,
    NAVER_PLACE,
    KAKAO,
    INSTAGRAM,
    YOUTUBE,
    BAEMIN
}
