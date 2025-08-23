package com.likelion.cleopatra.domain.openApi.exception;

import com.likelion.cleopatra.global.exception.CleopatraException;

public class OpenApiException extends CleopatraException {
    public OpenApiException(OpenApiErrorCode errorCode) { super(errorCode); }
}
