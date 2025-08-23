package com.likelion.cleopatra.domain.crwal.exception;

import com.likelion.cleopatra.global.exception.CleopatraException;
import lombok.Getter;

public class CrawlException extends CleopatraException {
    public CrawlException(CrawlErrorCode errorCode) { super(errorCode); }

    public CrawlErrorCode crawlErrorCode() { return (CrawlErrorCode) super.getErrorCode(); }

}
