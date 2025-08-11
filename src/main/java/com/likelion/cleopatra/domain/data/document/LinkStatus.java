package com.likelion.cleopatra.domain.data.document;

public enum LinkStatus {
    NEW,        // 발견만 됨(큐에 들어옴)
    FETCHING,   // "지금 가져오는 중" — 워커가 잡아서 처리 시작
    FETCHED,    // "가져오기 완료" — HTML/본문 저장까지 끝
    PARSED,     // "분석 완료" — NLP/키워드/5W1H 저장까지 끝
    FAILED      // 실패 — 재시도 대상 또는 보류
}