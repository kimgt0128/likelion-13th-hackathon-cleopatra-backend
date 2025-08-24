// src/main/java/com/likelion/cleopatra/global/common/enums/keyword/Primary.java
package com.likelion.cleopatra.global.common.enums.keyword;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Schema(
        name = "Primary",
        description = "1차 카테고리(대분류). 요청/응답은 한글 라벨만 사용.",
        example = "외식업",
        allowableValues = {"외식업","서비스업","도매업"}
)
@Getter
public enum Primary {
    FOOD_SERVICE("외식업"),
    SERVICE("서비스업"),
    WHOLESALE("도매업");

    private final String ko;
    Primary(String ko){ this.ko = ko; }

    @JsonValue
    public String toJson(){ return ko; }

    /** 한글만 허용 */
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static Primary fromJson(String raw){
        if (raw == null) return null;
        String s = raw.trim();
        for (Primary v : values()){
            if (v.ko.equals(s)) return v;
        }
        throw new IllegalArgumentException("지원하지 않는 1차 카테고리: " + raw);
    }
}
