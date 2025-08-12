package com.likelion.cleopatra.domain.common.enums.address;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Schema(
        name = "District",
        description = "행정구(구 단위). 요청/응답은 반드시 한글 라벨을 사용합니다.",
        example = "노원구",
        allowableValues = {"노원구","도봉구","기타"}
)
@Getter
public enum District {
    NOWON_GU("노원구"),
    DOBONG_GU("도봉구"),
    OTHER("기타");

    private final String ko; // 한글 라벨(요청/표시용)

    District(String ko) { this.ko = ko; }

    /** 요청(JSON)에서 들어올 때: 한글만 허용 (영문/코드 불가) */
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static District fromJson(String raw) {
        if (raw == null) return null;
        String s = raw.trim();
        for (District d : values()) {
            if (d.ko.equals(s)) return d; // 정확히 "노원구" 등만 허용
        }
        throw new IllegalArgumentException("지원하지 않는 구: " + raw);
    }

    /** 응답(JSON)으로 내보낼 때: 한글 라벨로 */
    @JsonValue
    public String toJson() {
        return ko;
    }

    /** DB 저장/인덱스용 코드 */
    public String code() { return name(); } // 예: NOWON_GU
}
