package com.likelion.cleopatra.domain.common.enums.addrdss;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum Neighborhood {
    GONGNEUNG_DONG("공릉동"),
    HAGYE_DONG("하계동"),
    OTHER_DONG("기타");

    private final String ko;

    Neighborhood(String ko) { this.ko = ko; }

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Neighborhood fromJson(String raw) {
        if (raw == null) return null;
        String s = raw.trim();
        for (Neighborhood n : values()) {
            if (n.ko.equals(s)) return n; // 정확히 "공릉동" 등만 허용
        }
        throw new IllegalArgumentException("지원하지 않는 동: " + raw);
    }

    @JsonValue
    public String toJson() { return ko; }

    public String code() { return name(); } // GONGNEUNG 등
}