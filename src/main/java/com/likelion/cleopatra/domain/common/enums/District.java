package com.likelion.cleopatra.domain.common.enums;

public enum District {
    NOWON_GU("노원구"),
    DOBONG_GU("도봉구"),
    OTHER("기타");

    private final String displayName;

    District(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
