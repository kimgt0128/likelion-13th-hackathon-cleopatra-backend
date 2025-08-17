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
public class NaverPlaceLinkRes {
    private String placeId;          // 추출된 placeId
    private String placeName;        // 상호명
    private String placeUrl;         // https://map.naver.com/p/search/%EA%B3%B5%EB%A6%89%20%EC%9D%BC%EC%8B%9D/place/1641655132
}