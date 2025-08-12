package com.likelion.cleopatra.domain.data.exception;

import com.likelion.cleopatra.global.exception.CleopatraException;

public class LinkCollectException extends CleopatraException {
    public LinkCollectException(LinkCollectErrorCode errorCode) { super(errorCode); }
}