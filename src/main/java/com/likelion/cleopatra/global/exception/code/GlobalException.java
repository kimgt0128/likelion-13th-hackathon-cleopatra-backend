package com.likelion.cleopatra.global.exception.code;

import com.likelion.cleopatra.global.exception.CleopatraException;

public class GlobalException extends CleopatraException {
    public GlobalException(GlobalErrorCode errorCode) {
        super(errorCode);
    }
}
