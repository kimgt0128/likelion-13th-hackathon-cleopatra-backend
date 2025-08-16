package com.likelion.cleopatra.domain.collect.exception;

import com.likelion.cleopatra.global.exception.code.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LinkCollectErrorCode implements ErrorCode {
    REQUEST_VALIDATION_FAILED(400, "요청 유효성 검사 실패"),
    MISSING_QUERY(400, "query 파라미터는 필수"),
    INVALID_DISPLAY_RANGE(400, "display는 10~100 범위"),
    INVALID_START_RANGE(400, "start는 1~1000 범위"),
    INVALID_SORT(400, "sort는 sim 또는 date만 허용"),
    INVALID_TARGET(400, "target 값이 유효하지 않음"),

    NO_BLOG_LINK_FOUND(404, "검색 결과에 블로그 링크 없음"),
    NO_PLACE_LINK_FOUND(404, "검색 결과에 플레이스 링크 없음"),
    DUPLICATE_LINK_DETECTED(409, "중복 링크 감지"),
    DOMAIN_BLOCKED(403, "수집 금지 도메인"),
    KEYWORD_BLOCKED(403, "금칙어 포함 요청"),

    INTERNAL_ERROR(500, "시스템 내부 오류");

    private final int status;
    private final String message;
}