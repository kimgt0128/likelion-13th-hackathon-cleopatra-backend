package com.likelion.cleopatra.domain.crwal.dto.place;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@Builder
@NoArgsConstructor
@Getter
public class NaverPlaceContentRes {
    private String placeId;          // 추출된 placeId
    private String placeName;        // 상호명
    private String placeUrl;         // m.place 방문자 리뷰 URL
    private List<NaverPlaceReview> reviews; // 최대 perReview개
}