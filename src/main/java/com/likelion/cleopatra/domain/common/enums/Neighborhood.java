package com.likelion.cleopatra.domain.common.enums;

public enum Neighborhood {
    GONGNEUNG_DONG("공릉동"),
    SANGYE_DONG("상계동"),
    OTHER("기타"); // 필요시 추가

    private final String displayName;

    Neighborhood(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}