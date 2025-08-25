package com.likelion.cleopatra.domain.keywordData.dto.webClient;

import com.likelion.cleopatra.domain.keywordData.dto.KeywordExtractReq;
import lombok.*;

import java.util.List;
import java.util.Map;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class KeywordDescriptionReq {

    private String area;    // 예: "노원구 공릉동"
    private String category; // 예: "공릉동 일식"

    // "data_naver_blog", "data_naver_palce", "data_youtube"
    private Map<String, List<Snippet>> data;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Snippet {
        private String platform; // NAVER_BLOG / NAVER_PLACE / YOUTUBE
        private String text;
    }

    /** 서비스에서 빌더 쓰지 않고 DTO 내부에서 생성 */
    public static KeywordDescriptionReq of(KeywordExtractReq req,
                                           Map<String, List<Snippet>> data) {
        String area = req.getDistrict().getKo() + " " + req.getNeighborhood().getKo();
        String category = req.getNeighborhood().getKo() + " " + req.getSecondary().getKo();
        return KeywordDescriptionReq.builder()
                .area(area)
                .category(category)
                .data(data)
                .build();
    }
}