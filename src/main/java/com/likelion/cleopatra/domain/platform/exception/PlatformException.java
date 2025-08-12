package com.likelion.cleopatra.domain.platform.exception;

import com.likelion.cleopatra.global.exception.CleopatraException;

public class PlatformException extends CleopatraException {
    public PlatformException(PlatformErrorCode errorCode) {
        super(errorCode);
    }
}
