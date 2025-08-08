package com.likelion.cleopatra.global.exception;

import com.likelion.cleopatra.global.exception.code.ErrorCode;
import lombok.Getter;

@Getter
public class CleopatraException extends RuntimeException {
    private final ErrorCode errorCode;

    public CleopatraException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
