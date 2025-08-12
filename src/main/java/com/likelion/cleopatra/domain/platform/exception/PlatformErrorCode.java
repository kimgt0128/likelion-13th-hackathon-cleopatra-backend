// enum을 int 상태 코드로 변경
package com.likelion.cleopatra.domain.platform.exception;

import com.likelion.cleopatra.global.exception.code.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PlatformErrorCode implements ErrorCode {

    // 헤더·토큰 유효성 관련
    MISSING_CLIENT_HEADERS(400, "필수 헤더 누락: X-Naver-Client-Id 또는 X-Naver-Client-Secret"),
    INVALID_CLIENT_CREDENTIALS(401, "네이버 클라이언트 자격증명 불일치"),
    UNAUTHORIZED_TOKEN(401, "액세스 토큰이 유효하지 않음"),
    FORBIDDEN_APP(403, "애플리케이션 권한 부족 또는 차단"),

    // 파라미터 관련
    MISSING_QUERY(400, "query 파라미터는 필수"),
    INVALID_DISPLAY_RANGE(400, "display는 10~100 범위"),
    INVALID_START_RANGE(400, "start는 1~1000 범위"),
    INVALID_SORT(400, "sort는 sim 또는 date만 허용"),
    INVALID_TARGET(400, "target 값이 유효하지 않음"),

    // 호출 제한
    RATE_LIMIT_EXCEEDED(429, "요청 속도 제한 초과"),
    QUOTA_EXCEEDED(429, "일/월 사용량 한도 초과"),

    // 네이버 OpenAPI 응답 오류
    NAVER_TIMEOUT(504, "네이버 OpenAPI 응답 지연"),
    NAVER_BAD_GATEWAY(502, "네이버 OpenAPI 통신 오류"),
    NAVER_SERVICE_UNAVAILABLE(503, "네이버 OpenAPI 서비스 불가"),
    NAVER_ERROR_RESPONSE(502, "네이버 오류 응답 반환(errorCode, errorMessage)"),

    // 응답 파싱/매핑
    RESPONSE_PARSING_ERROR(502, "응답 파싱 실패"),
    RESPONSE_SCHEMA_CHANGED(502, "응답 스키마 변경 감지"),

    // 비즈니스 규칙
    NO_BLOG_LINK_FOUND(404, "검색 결과에 블로그 링크 없음"),
    DUPLICATE_LINK_DETECTED(409, "중복 링크 감지"),
    DOMAIN_BLOCKED(403, "수집 금지 도메인"),
    KEYWORD_BLOCKED(403, "금칙어 포함 요청"),

    // 시스템
    REQUEST_VALIDATION_FAILED(400, "요청 유효성 검사 실패"),
    INTERNAL_ERROR(500, "시스템 내부 오류");

    private final int status;
    private final String message;
}
