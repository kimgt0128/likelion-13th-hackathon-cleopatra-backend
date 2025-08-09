package com.likelion.cleopatra.global.exception;

import com.likelion.cleopatra.global.exception.code.GlobalErrorCode;

public class GlobalException extends CleopatraException {
    public GlobalException(GlobalErrorCode errorCode) {
        super(errorCode);
    }
}
