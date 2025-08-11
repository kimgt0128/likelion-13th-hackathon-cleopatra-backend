package com.likelion.cleopatra.domain.data.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CollectResultRes {
    private final int inserted;   // 새로 들어간 문서 수
    private final String query;   // 실제 사용한 검색어(사후 추적용)
    private final Integer display;
    private final Integer start;
    private final long elapsedMs; // 처리 시간(ms)
}
