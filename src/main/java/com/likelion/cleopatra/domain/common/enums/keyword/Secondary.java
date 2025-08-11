// src/main/java/com/likelion/cleopatra/domain/common/enums/keyword/Secondary.java
package com.likelion.cleopatra.domain.common.enums.keyword;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

/**
 * 2차 카테고리(중분류).
 * - 직렬화(@JsonValue)는 한글 라벨로 나갑니다.
 * - 역직렬화(@JsonCreator)는 한글 라벨 또는 영문 enum 이름을 모두 허용합니다.
 *   예) "카페" -> CAFE, "CAFE" -> CAFE
 */
@Schema(
        name = "Secondary",
        description = "2차 카테고리(중분류)",
        example = "카페",
        allowableValues = {
                // 외식업
                "한식","중식","일식","양식","패스트푸드","카페","제과","술집","기타",
                // 서비스업
                "교육","의료업","스포츠","뷰티","문화","법률","기타 서비스업",
                // 도매업
                "의류","서적","식물","애완동물","안경","가구","스포츠"
        }
)
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
    EDUCATION("교육", Primary.SERVICE),        // 학원/교습 단순화
    MEDICAL("의료업", Primary.SERVICE),        // 병원/치과/한의원/약국 등 단순화
    SPORTS("스포츠", Primary.SERVICE),
    BEAUTY("뷰티", Primary.SERVICE),          // 화장품/네일/미용실 등
    CULTURE("문화", Primary.SERVICE),         // 만화방/오락 등
    LEGAL("법률", Primary.SERVICE),           // 회계사/공인중개사/변호사 등
    SERVICE_ETC("기타 서비스업", Primary.SERVICE),

    // ===== 도매업(WHOLESALE)
    CLOTHING("의류", Primary.WHOLESALE),       // 브랜드/신발 포함
    SPORTS_GOODS("스포츠", Primary.WHOLESALE), // 운동/경기 용품
    BOOKS("서적", Primary.WHOLESALE),
    PLANTS("식물", Primary.WHOLESALE),         // 꽃/나무
    PETS("애완동물", Primary.WHOLESALE),
    OPTICAL("안경", Primary.WHOLESALE),
    FURNITURE("가구", Primary.WHOLESALE);

    private final String ko;
    private final Primary primary;

    Secondary(String ko, Primary primary) {
        this.ko = ko;
        this.primary = primary;
    }

    /** JSON 직렬화: 한글 라벨로 내보냄 */
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
            if (v.ko.equals(s)) return v;               // 예: "카페"
            if (v.name().equalsIgnoreCase(s)) return v; // 예: "CAFE"
        }
        throw new IllegalArgumentException("지원하지 않는 2차 카테고리: " + raw);
    }

    /** 1차-2차 매칭 검증 */
    public boolean isAllowedFor(Primary pk) {
        return this.primary == pk;
    }
}
