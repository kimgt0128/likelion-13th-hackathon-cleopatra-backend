package com.likelion.cleopatra.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GlobalErrorCode implements ErrorCode{

    BAD_REQUEST(400, "잘못된 요청입니다"),
    METHOD_NOT_ALLOWED(405, "허용되지 않는 메소드입니다"),
    REQUEST_TIMEOUT(408, "요청 시간이 초과되었습니다"),

    PAYLOAD_TOO_LARGE(413, "요청 데이터가 너무 큽니다"),
    UNSUPPORTED_MEDIA_TYPE(415, "지원하지 않는 미디어 형식입니다"),
    INVALID_FILE_FORMAT(400, "지원하지 않는 파일 형식입니다"),
    FILE_TOO_LARGE(400, "파일 크기가 너무 큽니다"),

    INTERNAL_ERROR(500, "서버 내부 오류가 발생했습니다"),
    DATABASE_ERROR(500, "데이터베이스 오류가 발생했습니다"),
    SERVICE_UNAVAILABLE(503, "서비스를 이용할 수 없습니다");

    private final int status;
    private final String message;
}
