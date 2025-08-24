package com.likelion.cleopatra.global.common.enums.address;

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

    private final String ko;
    Neighborhood(String ko) { this.ko = ko; }

    /** 요청(JSON): 한글만 허용. '동' 생략 시 자동 보정 */
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Neighborhood fromJson(String raw) {
        if (raw == null) return null;
        String s = raw.trim();
        String norm = s.endsWith("동") ? s : s + "동";
        for (Neighborhood n : values()) {
            if (n.ko.equals(norm)) return n;
        }
        throw new IllegalArgumentException("지원하지 않는 동: " + raw);
    }

    /** 응답(JSON): 한글로 출력 */
    @JsonValue
    public String toJson() { return ko; }

    public String code() { return name(); }
}