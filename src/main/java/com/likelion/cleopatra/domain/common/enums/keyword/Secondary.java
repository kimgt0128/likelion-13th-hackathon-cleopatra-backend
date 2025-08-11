// src/main/java/com/likelion/cleopatra/domain/common/enums/keyword/Secondary.java
package com.likelion.cleopatra.domain.common.enums.keyword;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum Secondary {

    // ===== 외식업(FOOD_SERVICE)
    KOREAN("한식", Primary.FOOD_SERVICE),
    CHINESE("중식", Primary.FOOD_SERVICE),
    JAPANESE("일식", Primary.FOOD_SERVICE),
    WESTERN("양식", Primary.FOOD_SERVICE),
    FAST_FOOD("패스트푸드", Primary.FOOD_SERVICE),
    CAFE("카페", Primary.FOOD_SERVICE),
    BAKERY("제과", Primary.FOOD_SERVICE),
    PUB("술집", Primary.FOOD_SERVICE),
    FOOD_ETC("기타", Primary.FOOD_SERVICE),

    // ===== 서비스업(SERVICE)
    EDUCATION("교육", Primary.SERVICE),          // 학원/교습 등 단순화
    MEDICAL("의료업", Primary.SERVICE),          // 병원/치과/한의원/약국 등 단순화
    SPORTS("스포츠", Primary.SERVICE),           // 볼링/골프 등
    BEAUTY("뷰티", Primary.SERVICE),            // 화장품/네일/미용실 등
    CULTURE("문화", Primary.SERVICE),           // 만화방/비디오/오락 등
    LEGAL("법률", Primary.SERVICE),             // 회계사/공인중개사/변호사 등
    SERVICE_ETC("기타 서비스업", Primary.SERVICE),

    // ===== 도매업(WHOLESALE)
    CLOTHING("의류", Primary.WHOLESALE),         // 브랜드/신발 포함
    SPORTS_GOODS("스포츠", Primary.WHOLESALE),    // 운동/경기 용품
    BOOKS("서적", Primary.WHOLESALE),
    PLANTS("식물", Primary.WHOLESALE),           // 꽃/나무
    PETS("애완 동물", Primary.WHOLESALE),
    OPTICAL("안경", Primary.WHOLESALE),
    FURNITURE("가구", Primary.WHOLESALE);

    private final String ko;
    private final Primary primary;

    Secondary(String ko, Primary primary) {
        this.ko = ko;
        this.primary = primary;
    }

    /** JSON 직렬화: 한글 라벨로 */
    @JsonValue
    public String toJson() {
        return ko;
    }

    /** JSON 역직렬화: 한글 라벨 또는 enum name 허용 */
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Secondary from(String raw) {
        if (raw == null) return null;
        String s = raw.trim();
        if (s.isEmpty()) return null;

        for (Secondary v : values()) {
            if (v.ko.equals(s)) return v;               // "치킨" 같은 한글
            if (v.name().equalsIgnoreCase(s)) return v; // "CHICKEN" 같은 영문
        }
        throw new IllegalArgumentException("지원하지 않는 2차 카테고리: " + raw);
    }

    /** 1차-2차 허용 매칭 검증 */
    public boolean isAllowedFor(Primary pk) {
        return this.primary == pk;
    }
}
