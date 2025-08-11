package com.likelion.cleopatra.domain.data.document;

import com.likelion.cleopatra.domain.common.enums.District;
import com.likelion.cleopatra.domain.common.enums.Neighborhood;
import com.likelion.cleopatra.domain.common.enums.Platform;
import com.likelion.cleopatra.domain.data.util.UrlKey;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndexes({
        @CompoundIndex(name = "status_prio_disc_idx", def = "{'status':1, 'priority':-1, 'discoveredAt':1}"),
        @CompoundIndex(name = "region_idx", def = "{'district':1, 'neighborhood':1, 'discoveredAt':-1}"),
        @CompoundIndex(name = "category_idx", def = "{'categoryPrimary':1, 'categorySecondary':1, 'discoveredAt':-1}")
})
@Document(collection = "links")
public class LinkDoc {

    @Id
    private String id;

    @Indexed(unique = true)
    private String url;

    private String canonicalUrl;           // 정규화 URL
    private Platform platform;             // NAVER, KAKAO, BAEMIN 등

    // 태깅, 필터
    private String query;                  // 수집 검색어
    private String categoryPrimary;        // 요식업/서비스업/도매업
    private String categorySecondary;      // 예: 치킨, 한식, 일식...

    private District district;             // 구 (Enum 쓰던 그대로)
    private Neighborhood neighborhood;     // 동 (Enum 쓰던 그대로)

    private String postdateHint;           // "yyyyMMdd" (검색 응답값)

    // 큐 상태
    private LinkStatus status;             // NEW/FETCHING/...
    @Builder.Default private Integer priority = 5; // 큐 우선순위
    @Builder.Default private Integer tries = 0; // 실패 재시도 횟수
    private String lastError;              // 마지막 에러 메시지

    // 워커 잠금(옵션: 여러 워커 돌릴 때 유용)
    private String workerId;
    private Instant lockedUntil;

    // 타임 스탬프
    private Instant discoveredAt;          // 수집 시각
    private Instant updatedAt;             // 상태 변경 시각

    /** 네이버 블로그 검색 아이템 → LinkDoc (id/url/canonical 포함) */
    public static LinkDoc fromNaver(String url,
                                    String query,
                                    String categoryPrimary,
                                    String categorySecondary,
                                    District district,
                                    Neighborhood neighborhood) {
        String canonical = UrlKey.canonicalize(url);
        String id = UrlKey.idOf(Platform.NAVER_BLOG, canonical);
        Instant now = Instant.now();

        return LinkDoc.builder()
                .id(id)
                .url(url)
                .canonicalUrl(canonical)
                .platform(Platform.NAVER_BLOG)
                .query(query)
                .categoryPrimary(categoryPrimary)
                .categorySecondary(categorySecondary)
                .district(district)
                .neighborhood(neighborhood)
                .status(LinkStatus.NEW)
                .priority(5)
                .tries(0)
                .discoveredAt(now)
                .updatedAt(now)
                .build();
    }
}
