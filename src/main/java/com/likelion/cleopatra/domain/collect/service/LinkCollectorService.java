// LinkCollectorService.java
package com.likelion.cleopatra.domain.collect.service;

import com.likelion.cleopatra.domain.collect.document.LinkDoc;
import com.likelion.cleopatra.domain.collect.dto.requeset.CollectNaverLinkReq;
import com.likelion.cleopatra.domain.collect.dto.response.CollectResultRes;
import com.likelion.cleopatra.domain.collect.exception.LinkCollectErrorCode;
import com.likelion.cleopatra.domain.collect.exception.LinkCollectException;
import com.likelion.cleopatra.domain.collect.repository.LinkDocRepository;
import com.likelion.cleopatra.domain.openApi.naver.dto.NaverSearchRes;
import com.likelion.cleopatra.domain.openApi.naver.dto.blog.NaverBlogItem;
import com.likelion.cleopatra.domain.openApi.naver.dto.place.NaverPlaceItem;
import com.likelion.cleopatra.domain.openApi.naver.service.NaverApiService;
import com.likelion.cleopatra.global.common.enums.address.Neighborhood;
import com.likelion.cleopatra.global.common.enums.keyword.Secondary;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class LinkCollectorService {

    private final NaverApiService naverApiService;
    private final LinkDocRepository linkDocRepository;
    private final MongoTemplate mongoTemplate;

    /** 네이버 블로그 링크 수집/적재 */
    public CollectResultRes collectNaverBlogLinks(CollectNaverLinkReq req) {
        long t0 = System.currentTimeMillis();

        int display = Math.min(50, req.displayOrDefault());
        int start   = req.startOrDefault();

        validateRange(display, start, 10, 50, 1, 100);
        String query = buildQueryOrThrow(req.neighborhood(), req.secondary());

        NaverSearchRes<NaverBlogItem> res = naverApiService.searchBlog(query, display, start).block();
        if (res == null || res.getItems() == null) throw new LinkCollectException(LinkCollectErrorCode.NO_BLOG_LINK_FOUND);

        int inserted = (int) res.getItems().stream()
                .map(it -> LinkDoc.fromNaverBlog(
                        it.getLink(),
                        it.getPostdate(),
                        query,
                        req.primary(),
                        req.secondary(),
                        req.district(),
                        req.neighborhood()))
                .filter(this::insertIfAbsent)
                .count();

        log.info("Naver blog collected: query='{}' inserted={}, totalBatch={}", query, inserted, res.getItems().size());
        return new CollectResultRes(inserted, query, display, start, System.currentTimeMillis() - t0);
    }

    /** 네이버 플레이스 링크 수집/적재 */
    public CollectResultRes collectNaverPlaceLinks(CollectNaverLinkReq req) {
        long t0 = System.currentTimeMillis();

        int display = Math.min(5, Math.max(1, req.displayOrDefault())); // API 제약
        int start   = 1;                                                // API 제약(문서상 1만 유효)

        validateRange(display, start, 1, 5, 1, 1);
        String query = buildQueryOrThrow(req.neighborhood(), req.secondary());

        NaverSearchRes<NaverPlaceItem> res = naverApiService.searchPlace(query, display, start, "random").block();
        if (res == null || res.getItems() == null) throw new LinkCollectException(LinkCollectErrorCode.NO_PLACE_LINK_FOUND);

        int inserted = (int) res.getItems().stream()
                .peek(it -> log.debug("PLACE item title='{}' link='{}' road='{}' addr='{}'",
                        it.getTitle(), it.getLink(), it.getRoadAddress(), it.getAddress()))
                .map(it -> LinkDoc.fromNaverPlace(
                        it,
                        query,
                        req.primary(),
                        req.secondary(),
                        req.district(),
                        req.neighborhood()))
                .filter(this::insertIfAbsent)
                .count();

        log.info("Naver place collected: query='{}' inserted={}, totalBatch={}", query, inserted, res.getItems().size());
        return new CollectResultRes(inserted, query, display, start, System.currentTimeMillis() - t0);
    }

    /* ---------------- upsert 공통 ---------------- */
    private boolean insertIfAbsent(LinkDoc doc) {
        Query q = Query.query(Criteria.where("_id").is(doc.getId()));
        Update u = new Update()
                .setOnInsert("_id", doc.getId())
                .setOnInsert("url", doc.getUrl())
                .setOnInsert("canonicalUrl", doc.getCanonicalUrl())
                .setOnInsert("platform", doc.getPlatform())
                .setOnInsert("query", doc.getQuery())
                .setOnInsert("categoryPrimary", doc.getCategoryPrimary())
                .setOnInsert("categorySecondary", doc.getCategorySecondary())
                .setOnInsert("district", doc.getDistrict())
                .setOnInsert("neighborhood", doc.getNeighborhood())
                .setOnInsert("postdateHint", doc.getPostdateHint())
                .setOnInsert("placeTitle", doc.getPlaceTitle())
                .setOnInsert("placeCategory", doc.getPlaceCategory())
                .setOnInsert("placePhone", doc.getPlacePhone())
                .setOnInsert("placeAddr", doc.getPlaceAddr())
                .setOnInsert("placeRoadAddr", doc.getPlaceRoadAddr())
                .setOnInsert("placeLon", doc.getPlaceLon())
                .setOnInsert("placeLat", doc.getPlaceLat())
                .setOnInsert("placeId", doc.getPlaceId())
                .setOnInsert("status", doc.getStatus())
                .setOnInsert("priority", doc.getPriority())
                .setOnInsert("tries", doc.getTries())
                .setOnInsert("discoveredAt", doc.getDiscoveredAt())
                .setOnInsert("updatedAt", doc.getUpdatedAt());

        UpdateResult r = mongoTemplate.upsert(q, u, LinkDoc.class);
        return r.getUpsertedId() != null;
    }

    /* ---------------- 공통 검증 ---------------- */

    private void validateRange(int display, int start, int minDisplay, int maxDisplay, int minStart, int maxStart) {
        if (display < minDisplay || display > maxDisplay)
            throw new LinkCollectException(LinkCollectErrorCode.INVALID_DISPLAY_RANGE);
        if (start < minStart || start > maxStart)
            throw new LinkCollectException(LinkCollectErrorCode.INVALID_START_RANGE);
    }

    private String buildQueryOrThrow(Neighborhood neighborhood, Secondary secondary) {
        if (neighborhood == null || secondary == null)
            throw new LinkCollectException(LinkCollectErrorCode.REQUEST_VALIDATION_FAILED);
        return neighborhood.getKo() + " " + secondary.getKo();
    }
}
