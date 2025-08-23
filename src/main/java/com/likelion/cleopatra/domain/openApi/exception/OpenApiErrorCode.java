// enum을 int 상태 코드로 변경
package com.likelion.cleopatra.domain.openApi.exception;

import com.likelion.cleopatra.global.exception.code.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OpenApiErrorCode implements ErrorCode {
    MISSING_CLIENT_HEADERS(400, "필수 헤더 누락: X-Naver-Client-Id 또는 X-Naver-Client-Secret"),
    INVALID_CLIENT_CREDENTIALS(401, "네이버 클라이언트 자격증명 불일치"),
    UNAUTHORIZED_TOKEN(401, "액세스 토큰이 유효하지 않음"),
    FORBIDDEN_APP(403, "애플리케이션 권한 부족 또는 차단"),

    RATE_LIMIT_EXCEEDED(429, "요청 속도 제한 초과"),
    QUOTA_EXCEEDED(429, "일/월 사용량 한도 초과"),

    NAVER_TIMEOUT(504, "네이버 OpenAPI 응답 지연"),
    NAVER_BAD_GATEWAY(502, "네이버 OpenAPI 통신 오류"),
    NAVER_SERVICE_UNAVAILABLE(503, "네이버 OpenAPI 서비스 불가"),
    NAVER_ERROR_RESPONSE(502, "네이버 오류 응답 반환(errorCode, errorMessage)"),

    RESPONSE_PARSING_ERROR(502, "응답 파싱 실패"),
    RESPONSE_SCHEMA_CHANGED(502, "응답 스키마 변경 감지");

    private final int status;
    private final String message;
}