package com.likelion.cleopatra.domain.collect.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
@Schema(name = "CollectResultRes", description = "수집 결과 요약")
public class CollectResultRes {

    @Schema(description = "신규 삽입된 링크 수", example = "12")
    private final int inserted;

    @Schema(description = "실제로 사용된 검색 쿼리", example = "공릉동 카페")
    private final String query;

    @Schema(description = "적용된 display(최대 100)", example = "50")
    private final Integer display;

    @Schema(description = "적용된 start", example = "1")
    private final Integer start;

    @Schema(description = "처리 시간(ms)", example = "245")
    private final long elapsedMs;

    public CollectResultRes from(int inserted, String query, int display, int start, long elapsedMs) {
        return CollectResultRes.builder()
                .inserted(inserted)
                .query(query)
                .display(display)
                .start(start)
                .elapsedMs(elapsedMs).build();
    }

}
