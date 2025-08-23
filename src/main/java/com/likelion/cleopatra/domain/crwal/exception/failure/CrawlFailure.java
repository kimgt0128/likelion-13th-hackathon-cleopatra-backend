package com.likelion.cleopatra.domain.crwal.exception.failure;

import com.likelion.cleopatra.domain.crwal.exception.CrawlErrorCode;

public record CrawlFailure(CrawlErrorCode code, String message, String cause) {}