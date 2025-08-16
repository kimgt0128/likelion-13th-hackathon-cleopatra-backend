package com.likelion.cleopatra.domain.collect.document;

import com.likelion.cleopatra.global.common.enums.address.District;
import com.likelion.cleopatra.global.common.enums.address.Neighborhood;
import com.likelion.cleopatra.global.common.enums.Platform;
import com.likelion.cleopatra.global.common.enums.keyword.Primary;
import com.likelion.cleopatra.global.common.enums.keyword.Secondary;
import com.likelion.cleopatra.domain.collect.util.UrlKey;
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

/**
 * 정렬 기준과 인덱스 설계 메모
 *
 * 1) 크롤 배치 조회(핵심 경로)
 *    - 쿼리:  platform = ? AND status = ?
 *    - 정렬:  priority DESC, updatedAt ASC
 *    - limit: 50
 *    => 인덱스: { platform:1, status:1, priority:-1, updatedAt:1 }
 *       - 이유:
 *         * 동등 매칭 필드(platform, status)는 인덱스의 "앞부분"에 배치(선행키 프리픽스 규칙).
 *         * 정렬 필드(priority, updatedAt)를 "뒤"에 배치하면 인덱스만으로 소트 커버 가능.
 *         * priority는 내림차순(-1), updatedAt은 오름차순(1)으로 실제 정렬 방향과 동일하게 둔다.
 *         * 이렇게 하면 메모리 소트 없이 상위 50건을 바로 읽는다.
 *
 * 2) 지역/동 최신 조회(보조 경로)
 *    - 쿼리:  district = ? AND neighborhood = ?
 *    - 정렬:  discoveredAt DESC
 *    => 인덱스: { district:1, neighborhood:1, discoveredAt:-1 }
 *       - 이유:
 *         * 지역+동으로 필터링 후 최신순(내림차순) 정렬을 인덱스로 커버.
 *
 * 3) 카테고리 최신 조회(보조 경로)
 *    - 쿼리:  categoryPrimary = ? AND categorySecondary = ?
 *    - 정렬:  discoveredAt DESC
 *    => 인덱스: { categoryPrimary:1, categorySecondary:1, discoveredAt:-1 }
 *
 * 4) 주의사항
 *    - 인덱스는 쓰기 비용과 저장 공간을 증가시킨다. 실제 조회 패턴 3가지만 남겼다.
 *    - 정렬 방향이 바뀌면(ASC/DESC) 인덱스 커버리지가 깨질 수 있다. 정렬을 바꾸지 말 것.
 *    - 복합 인덱스는 "왼쪽 프리픽스"만 효과적이다.
 *      예) {A,B,C} 인덱스는 (A), (A,B), (A,B,C) 쿼리엔 효과적이나 (B,C) 단독에겐 비효율.
 *    - 배치 쿼리는 항상 limit(예: 50)을 걸어 네트워크 전송량과 서버 부하를 줄인다.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndexes({
        // 배치 쿼리용: findByPlatformAndStatus(..., sort = priority desc, updatedAt asc)
        // 정렬 커버리지 확보를 위해 인덱스의 정렬 방향을 쿼리 정렬과 동일하게 맞춘다.
        @CompoundIndex(
                name = "plat_stat_pri_upd",
                def = "{'platform':1, 'status':1, 'priority':-1, 'updatedAt':1}"
        ),

        // 지역/동 최신 글 탐색. 최신순(discoveredAt desc) 정렬을 인덱스로 커버.
        @CompoundIndex(
                name = "region_idx",
                def = "{'district':1, 'neighborhood':1, 'discoveredAt':-1}"
        ),

        // 카테고리 최신 글 탐색. 최신순(discoveredAt desc) 정렬을 인덱스로 커버.
        @CompoundIndex(
                name = "category_idx",
                def = "{'categoryPrimary':1, 'categorySecondary':1, 'discoveredAt':-1}"
        )
})
@Document(collection = "links")
public class LinkDoc {

    @Id
    private String id;

    @Indexed(unique = true)
    private String url;

    private String canonicalUrl;           // 정규화 URL
    private Platform platform;             // NAVER_BLOG, NAVER_PLACE, KAKAO_...

    // 태깅, 필터
    private String query;                  // 수집 검색어
    private String categoryPrimary;        // 요식업/서비스업/도매업
    private String categorySecondary;      // 예: 치킨, 한식, 일식...

    private District district;             // 구
    private Neighborhood neighborhood;     // 동

    private String postdateHint;           // "yyyyMMdd" (검색 응답값)

    // 큐 상태
    private LinkStatus status;             // NEW → FETCHING → FETCHED/PARSED/FAILED
    @Builder.Default private Integer priority = 5; // 큐 우선순위(높을수록 먼저 처리)
    @Builder.Default private Integer tries = 0;    // 실패 재시도 횟수
    private String lastError;              // 마지막 에러 메시지

    // 잠금(멀티 워커 대비 선택)
    private String workerId;
    private Instant lockedUntil;

    // 타임 스탬프
    private Instant discoveredAt;          // 수집 시각
    private Instant updatedAt;             // 상태 변경 시각

    // 상태 전환 도메인 메서드
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


    /** 네이버 블로그 검색 아이템 → LinkDoc (id/url/canonical 포함) */
    public static LinkDoc fromNaver(
            String url,
            String query,
            Platform platform,
            Primary primary,
            Secondary secondary,
            District district,
            Neighborhood neighborhood
    ) {
        String canonical = UrlKey.canonicalize(url);
        String id = UrlKey.idOf(platform, canonical);
        Instant now = Instant.now();

        return LinkDoc.builder()
                .id(id)
                .url(url)
                .canonicalUrl(canonical)
                .platform(platform)
                .query(query)
                .categoryPrimary(primary != null ? primary.getKo() : null)       // DB에는 한글 저장
                .categorySecondary(secondary != null ? secondary.getKo() : null) // DB에는 한글 저장
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
