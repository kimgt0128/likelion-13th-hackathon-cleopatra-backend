package com.likelion.cleopatra.domain.crwal.exception.failure;

import com.likelion.cleopatra.domain.crwal.exception.CrawlErrorCode;
import com.likelion.cleopatra.domain.crwal.exception.CrawlException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FailureClassifier {

    public CrawlFailure classify(Throwable t) {
        Throwable r = root(t);
        String msg = safe(r.getMessage());
        String low = msg.toLowerCase();

        if (t instanceof CrawlException ce)
            return new CrawlFailure(ce.crawlErrorCode() , ce.getMessage(), clip(msg, 300));

        if (t instanceof com.microsoft.playwright.TimeoutError)
            return new CrawlFailure(CrawlErrorCode.NAVIGATION_TIMEOUT, "페이지 로딩 제한시간 초과", clip(msg, 300));

        if (r instanceof java.net.UnknownHostException || low.contains("err_name_not_resolved"))
            return new CrawlFailure(CrawlErrorCode.NETWORK_IO_ERROR, "DNS 해석 실패", clip(msg, 300));
        if (r instanceof java.net.SocketTimeoutException || low.contains("err_connection_timed_out"))
            return new CrawlFailure(CrawlErrorCode.NETWORK_IO_ERROR, "연결 타임아웃", clip(msg, 300));
        if (r instanceof java.net.ConnectException || low.contains("err_connection_reset"))
            return new CrawlFailure(CrawlErrorCode.NETWORK_IO_ERROR, "연결 실패", clip(msg, 300));

        if (low.contains("429") || low.contains("rate limit"))
            return new CrawlFailure(CrawlErrorCode.RATE_LIMITED, "요청 제한", clip(msg, 300));
        if (low.contains("403") || low.contains("forbidden"))
            return new CrawlFailure(CrawlErrorCode.ACCESS_FORBIDDEN, "접근 권한 없음", clip(msg, 300));
        if (low.contains("404") || low.contains("not found"))
            return new CrawlFailure(CrawlErrorCode.NOT_FOUND, "대상 문서 없음", clip(msg, 300));
        if (low.contains("captcha") || low.contains("bot"))
            return new CrawlFailure(CrawlErrorCode.CAPTCHA_OR_BOT_DETECTED, "캡차/봇 탐지 차단", clip(msg, 300));

        if (low.contains("selector") || low.contains("se-main-container"))
            return new CrawlFailure(CrawlErrorCode.SELECTOR_MAIN_BODY_NOT_FOUND, "본문 컨테이너 선택자 불일치", clip(msg, 300));
        if (low.contains("empty"))
            return new CrawlFailure(CrawlErrorCode.CONTENT_EMPTY, "본문이 비어 있음", clip(msg, 300));

        return new CrawlFailure(CrawlErrorCode.UNKNOWN, "알 수 없는 크롤링 오류", clip(msg, 300));
    }

    private Throwable root(Throwable t){ while(t.getCause()!=null && t.getCause()!=t) t=t.getCause(); return t; }
    private String safe(String s){ return s==null? "" : s.replaceAll("\\s+"," ").trim(); }
    private String clip(String s,int n){ return s.length()<=n? s : s.substring(0,n); }
}