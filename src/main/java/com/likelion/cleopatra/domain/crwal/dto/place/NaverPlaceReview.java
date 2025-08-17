package com.likelion.cleopatra.domain.crwal.dto.place;

import lombok.*;

import java.util.List;
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor

/** 플레이스 '방문자 리뷰' 한 건 */
public class NaverPlaceReview {
    /** 상단 방문 키워드: 예) 점심에 방문, 예약 없이 이용 */
    private List<String> visitKeywords;
    /** 리뷰 본문 텍스트 */
    private String body;
    /** 하단 몇 번째 방문: 예) 2번째 방문 */
    private String revisit;
    /** 하단 만족 태그 칩: 예) 음식이 맛있어요, 재료가 신선해요 */
    private List<String> tags;
}