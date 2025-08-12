package com.likelion.cleopatra.domain.platform.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PlatformErrorCode {

    // 헤더·토큰 유효성 관련
    MISSING_CLIENT_HEADERS(HttpStatus.BAD_REQUEST, "필수 헤더 누락: X-Naver-Client-Id 또는 X-Naver-Client-Secret"),
    INVALID_CLIENT_CREDENTIALS(HttpStatus.UNAUTHORIZED, "네이버 클라이언트 자격증명 불일치"),
    UNAUTHORIZED_TOKEN(HttpStatus.UNAUTHORIZED, "액세스 토큰이 유효하지 않음"),
    FORBIDDEN_APP(HttpStatus.FORBIDDEN, "애플리케이션 권한 부족 또는 차단"),

    // 파라미터 관련
    MISSING_QUERY(HttpStatus.BAD_REQUEST, "query 파라미터는 필수"),
    INVALID_DISPLAY_RANGE(HttpStatus.BAD_REQUEST, "display는 10~100 범위"),
    INVALID_START_RANGE(HttpStatus.BAD_REQUEST, "start는 1~1000 범위"),
    INVALID_SORT(HttpStatus.BAD_REQUEST, "sort는 sim 또는 date만 허용"),
    INVALID_TARGET(HttpStatus.BAD_REQUEST, "target 값이 유효하지 않음"),

    // 호출 제한
    RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "요청 속도 제한 초과"),
    QUOTA_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "일/월 사용량 한도 초과"),

    // 네이버 OpenAPI 응답 오류
    NAVER_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, "네이버 OpenAPI 응답 지연"),
    NAVER_BAD_GATEWAY(HttpStatus.BAD_GATEWAY, "네이버 OpenAPI 통신 오류"),
    NAVER_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "네이버 OpenAPI 서비스 불가"),
    NAVER_ERROR_RESPONSE(HttpStatus.BAD_GATEWAY, "네이버 오류 응답 반환(errorCode, errorMessage)"),

    // 응답 파싱/매핑
    RESPONSE_PARSING_ERROR(HttpStatus.BAD_GATEWAY, "응답 파싱 실패"),
    RESPONSE_SCHEMA_CHANGED(HttpStatus.BAD_GATEWAY, "응답 스키마 변경 감지"),

    // 비즈니스 규칙
    NO_BLOG_LINK_FOUND(HttpStatus.NOT_FOUND, "검색 결과에 블로그 링크 없음"),
    DUPLICATE_LINK_DETECTED(HttpStatus.CONFLICT, "중복 링크 감지"),
    DOMAIN_BLOCKED(HttpStatus.FORBIDDEN, "수집 금지 도메인"),
    KEYWORD_BLOCKED(HttpStatus.FORBIDDEN, "금칙어 포함 요청"),

    // 시스템
    REQUEST_VALIDATION_FAILED(HttpStatus.BAD_REQUEST, "요청 유효성 검사 실패"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "시스템 내부 오류");

    private final HttpStatus httpStatus;
    private final String message;
}
