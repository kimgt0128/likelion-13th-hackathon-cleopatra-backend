package com.likelion.cleopatra.domain.crwal.exception;

import com.likelion.cleopatra.global.exception.code.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CrawlErrorCode implements ErrorCode {

    /* 입력/사전 검증 */
    INVALID_URL(400, "잘못된 URL"),
    UNSUPPORTED_PLATFORM(400, "지원하지 않는 플랫폼"),
    NO_LINKS_TO_CRAWL(404, "크롤링할 링크 없음"),

    /* 네트워크/탐색 */
    NAVIGATION_TIMEOUT(504, "페이지 로딩 제한시간 초과"),
    NETWORK_IO_ERROR(502, "네트워크/연결 오류"),
    NOT_FOUND(404, "대상 문서 없음"),
    ACCESS_FORBIDDEN(403, "접근 권한 없음"),
    RATE_LIMITED(429, "요청 제한"),

    /* 안티봇/차단 */
    CAPTCHA_OR_BOT_DETECTED(403, "캡차/봇 탐지 차단"),
    PAGE_BLOCKED(451, "정책에 의한 접근 차단"),

    /* 추출/파싱 */
    SELECTOR_MAIN_BODY_NOT_FOUND(422, "본문 컨테이너 선택자 불일치"),
    EXTRACT_TITLE_FAILED(424, "제목 추출 실패"),
    CONTENT_EMPTY(422, "본문이 비어 있음"),
    CONTENT_TOO_SHORT(422, "본문 길이 부족"),
    SCRIPT_EVAL_ERROR(500, "스크립트 실행 실패"),

    /* 동시성/중복 */
    CLAIM_LOCKED(423, "작업 잠금 상태"),
    DUPLICATE_URL(409, "중복 URL"),

    /* 저장 */
    STORAGE_SAVE_FAILED(500, "콘텐츠 저장 실패"),

    /* 기타 */
    UNKNOWN(500, "알 수 없는 크롤링 오류");

    private final int status;
    private final String message;
}
