package com.likelion.cleopatra.domain.collect.document;

import com.likelion.cleopatra.domain.collect.util.UrlKey;
import com.likelion.cleopatra.domain.openApi.naver.dto.place.NaverPlaceItem;
import com.likelion.cleopatra.global.common.enums.Platform;
import com.likelion.cleopatra.global.common.enums.address.District;
import com.likelion.cleopatra.global.common.enums.address.Neighborhood;
import com.likelion.cleopatra.global.common.enums.keyword.Primary;
import com.likelion.cleopatra.global.common.enums.keyword.Secondary;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
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
        @CompoundIndex(
                name = "plat_stat_pri_upd",
                def  = "{'platform':1, 'status':1, 'priority':-1, 'updatedAt':1}"
        ),
        @CompoundIndex(
                name = "region_idx",
                def  = "{'district':1, 'neighborhood':1, 'discoveredAt':-1}"
        ),
        @CompoundIndex(
                name = "category_idx",
                def  = "{'categoryPrimary':1, 'categorySecondary':1, 'discoveredAt':-1}"
        )
})
@Document(collection = "links")
public class LinkDoc {

    @Id
    private String id;

    @Indexed(unique = true)
    private String url;                 // 원본 URL(없을 수도 있음; 플레이스는 외부링크일 수 있음)

    private String canonicalUrl;        // 정규화 키(중복제거 기준)
    private Platform platform;          // NAVER_BLOG, NAVER_PLACE, ...

    // 공통 태깅
    private String query;               // 수집 검색어(예: "공릉동 일식")
    private String categoryPrimary;     // 예: 외식업
    private String categorySecondary;   // 예: 일식

    private District district;          // 구
    private Neighborhood neighborhood;  // 동

    // 블로그 전용 힌트
    private String postdateHint;        // yyyyMMdd

    // 플레이스 탐색 보조 메타(검색→정규 URL 발견 전 매칭 정확도↑용)
    private String placeTitle;          // 태그 제거된 상호명
    private String placeCategory;       // "술집>맥주,호프" 등
    private String placePhone;          // 전화
    private String placeAddr;           // 지번
    private String placeRoadAddr;       // 도로명
    private Double placeLon;            // WGS84 경도
    private Double placeLat;            // WGS84 위도
    private String placeId;             // m.place.naver.com/{cat}/{placeId}/... 추출 후 갱신

    // 큐 상태
    private LinkStatus status;
    @Builder.Default private Integer priority = 5;
    @Builder.Default private Integer tries = 0;
    private String lastError;

    // 잠금
    private String workerId;
    private Instant lockedUntil;

    // 타임스탬프
    private Instant discoveredAt;
    private Instant updatedAt;

    /* ---------------- 상태 전환 ---------------- */

    public void markFetching(Instant now) {
        this.status = LinkStatus.FETCHING;
        this.tries = (this.tries == null ? 0 : this.tries) + 1;
        this.updatedAt = now;
    }

    public void markFetched(Instant now) {
        this.status = LinkStatus.FETCHED;
        this.lastError = null;
        this.updatedAt = now;
    }

    public void markFailed(Instant now, String error) {
        this.status = LinkStatus.FAILED;
        this.lastError = error;
        this.updatedAt = now;
    }

    public void claim(String workerId, Instant until) {
        this.workerId = workerId;
        this.lockedUntil = until;
    }

    /* ---------------- 팩토리: 블로그 ---------------- */

    public static LinkDoc fromNaverBlog(
            String url,
            String postdateHint,
            String query,
            Primary primary,
            Secondary secondary,
            District district,
            Neighborhood neighborhood
    ) {
        String canonical = UrlKey.canonicalize(url);
        String id = UrlKey.idOf(Platform.NAVER_BLOG, canonical);
        Instant now = Instant.now();

        return LinkDoc.builder()
                .id(id)
                .url(url)
                .canonicalUrl(canonical)
                .platform(Platform.NAVER_BLOG)
                .query(query)
                .categoryPrimary(primary != null ? primary.getKo() : null)
                .categorySecondary(secondary != null ? secondary.getKo() : null)
                .district(district)
                .neighborhood(neighborhood)
                .postdateHint(postdateHint)
                .status(LinkStatus.NEW)
                .priority(5)
                .tries(0)
                .discoveredAt(now)
                .updatedAt(now)
                .build();
    }

    /* ---------------- 팩토리: 플레이스 ---------------- */

    public static LinkDoc fromNaverPlace(
            NaverPlaceItem item,
            String query,
            Primary primary,
            Secondary secondary,
            District district,
            Neighborhood neighborhood
    ) {
        String rawTitle = safe(stripTags(item.getTitle()));
        String rawLink  = safe(item.getLink());
        String road     = safe(item.getRoadAddress());
        String addr     = safe(item.getAddress());

        // canonical: 네이버 지도 링크가 있으면 그것으로, 없으면 title+roadAddress 조합으로 합성키 생성
        String canonicalSeed = (rawLink != null && (rawLink.contains("m.place.naver.com") || rawLink.contains("map.naver.com")))
                ? rawLink
                : ("local://" + rawTitle + "|" + road);

        String canonical = UrlKey.canonicalize(canonicalSeed);
        String id = UrlKey.idOf(Platform.NAVER_PLACE, canonical);

        Double lon = toCoord(item.getMapx()); // 1270779965 -> 127.0779965
        Double lat = toCoord(item.getMapy()); // 376236063  -> 37.6236063

        Instant now = Instant.now();

        return LinkDoc.builder()
                .id(id)
                .url(rawLink) // 없을 수도 있음
                .canonicalUrl(canonical)
                .platform(Platform.NAVER_PLACE)
                .query(query)
                .categoryPrimary(primary != null ? primary.getKo() : null)
                .categorySecondary(secondary != null ? secondary.getKo() : null)
                .district(district)
                .neighborhood(neighborhood)
                .placeTitle(rawTitle)
                .placeCategory(safe(item.getCategory()))
                .placePhone(emptyToNull(item.getTelephone()))
                .placeAddr(addr)
                .placeRoadAddr(road)
                .placeLon(lon)
                .placeLat(lat)
                .status(LinkStatus.NEW)
                .priority(5)
                .tries(0)
                .discoveredAt(now)
                .updatedAt(now)
                .build();
    }

    /* ---------------- 유틸 ---------------- */

    private static String stripTags(String s) {
        return s == null ? null : s.replaceAll("<[^>]+>", "");
    }
    private static String safe(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
    private static String emptyToNull(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
    private static Double toCoord(String v) {
        try {
            if (v == null || v.isBlank()) return null;
            long raw = Long.parseLong(v.trim());
            return raw / 1e7;
        } catch (Exception e) {
            return null;
        }
    }
}
