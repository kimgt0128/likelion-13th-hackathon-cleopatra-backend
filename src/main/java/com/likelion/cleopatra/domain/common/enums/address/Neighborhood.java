package com.likelion.cleopatra.domain.common.enums.address;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Schema(
        name = "Neighborhood",
        description = "행정동. 요청/응답은 반드시 한글 라벨을 사용합니다.",
        example = "공릉동",
        allowableValues = {"공릉동","하계동","기타"}
)
@Getter
public enum Neighborhood {
    GONGNEUNG_DONG("공릉동"),
    HAGYE_DONG("하계동"),
    OTHER_DONG("기타");

    private final String ko; // 한글 라벨(요청/표시/검색어 생성용)

    Neighborhood(String ko) { this.ko = ko; }

    /** JSON -> Enum : 한글만 받는다. ("공릉동", "하계동") */
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Neighborhood fromJson(String raw) {
        if (raw == null) return null;
        String s = raw.trim();
        // '동' 생략 허용: "공릉" -> "공릉동"
        String norm = s.endsWith("동") ? s : s + "동";
        for (Neighborhood n : values()) {
            if (n.ko.equals(norm)) return n;
        }
        throw new IllegalArgumentException("지원하지 않는 동: " + raw);
    }

    /** Enum -> JSON : 한글 라벨로 나간다. */
    @JsonValue
    public String toJson() { return ko; }

    /** 저장/인덱스용 코드 (DB에는 이 값을 사용) */
    public String code() { return name(); } // 예: GONGNEUNG_DONG
}
