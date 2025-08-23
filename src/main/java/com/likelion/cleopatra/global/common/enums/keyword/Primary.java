// src/main/java/com/likelion/cleopatra/domain/common/enums/keyword/Primary.java
package com.likelion.cleopatra.global.common.enums.keyword;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;


/**
 * 1차 카테고리(대분류).
 * - 직렬화(@JsonValue)는 한글 라벨(외식업/서비스업/도매업)로 나갑니다.
 * - 역직렬화(@JsonCreator)는 한글 라벨 또는 영문 enum 이름(FOOD_SERVICE/SERVICE/WHOLESALE) 모두 허용합니다.
 */
@Schema(
        name = "Primary",
        description = "1차 카테고리(대분류)",
        example = "외식업",
        allowableValues = {"외식업", "서비스업", "도매업"}
)
@Getter
public enum Primary {

    FOOD_SERVICE("외식업"),
    SERVICE("서비스업"),
    WHOLESALE("도매업");

    private final String ko;

    Primary(String ko) {
        this.ko = ko;
    }

    /** JSON 직렬화: 한글 라벨로 나가게 */
    @JsonValue
    public String toJson() {
        return ko;
    }

    /** JSON 역직렬화: 한글 라벨 또는 enum name 허용 */
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Primary from(String raw) {
        if (raw == null) return null;
        String s = raw.trim();
        if (s.isEmpty()) return null;

        for (Primary p : values()) {
            if (p.ko.equals(s)) return p;                // "외식업"
            if (p.name().equalsIgnoreCase(s)) return p;  // "FOOD_SERVICE"
        }
        throw new IllegalArgumentException("지원하지 않는 1차 카테고리: " + raw);
    }
}
