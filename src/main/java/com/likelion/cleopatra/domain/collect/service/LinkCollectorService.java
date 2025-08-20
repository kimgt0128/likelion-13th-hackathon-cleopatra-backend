package com.likelion.cleopatra.domain.collect.service;

import com.likelion.cleopatra.domain.collect.document.LinkDoc;
import com.likelion.cleopatra.domain.collect.dto.requeset.CollectNaverLinkReq;
import com.likelion.cleopatra.domain.collect.dto.response.CollectResultRes;
import com.likelion.cleopatra.domain.collect.exception.LinkCollectErrorCode;
import com.likelion.cleopatra.domain.collect.exception.LinkCollectException;
import com.likelion.cleopatra.domain.collect.repository.LinkDocRepository;
import com.likelion.cleopatra.domain.crwal.dto.CrawlRes;
import com.likelion.cleopatra.domain.crwal.dto.place.NaverPlaceLinkRes;
import com.likelion.cleopatra.domain.crwal.impl.NaverPlaceCrawler;
import com.likelion.cleopatra.domain.openApi.naver.dto.NaverSearchRes;
import com.likelion.cleopatra.domain.openApi.naver.dto.blog.NaverBlogItem;
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

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class LinkCollectorService {

    private final NaverApiService naverApiService;
    private final LinkDocRepository linkDocRepository;
    private final NaverPlaceCrawler naverPlaceCrawler;
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

    /** 키워드로 플레이스 링크만 수집해 LinkDoc 저장. HTML 일절 저장 안 함. */
    public CrawlRes naverPlaceCrawl(String keyword, int places, int perIgnored) {
        int placeLimit = Math.max(1, Math.min(50, places)); // 안전 상한

        List<NaverPlaceLinkRes> found = naverPlaceCrawler.crawlLinks(keyword, placeLimit);
        int picked = found.size();
        int success = 0, failed = 0;

        // 개별 저장하여 성공/실패 카운트
        for (NaverPlaceLinkRes r : found) {
            try {
                LinkDoc doc = LinkDoc.fromNaverPlaceLink(
                        r.getPlaceId(),
                        r.getPlaceName(),
                        r.getPlaceUrl(), // https://map.naver.com/p/search/{query}/place/{placeId}
                        keyword,
                        null, null, null, null // 필요 시 주입
                );
                log.debug("NAVER_PLACE before-save id={} name='{}' url={}",
                        r.getPlaceId(), r.getPlaceName(), r.getPlaceUrl());

                linkDocRepository.save(doc); // id가 동일하면 업데이트, 아니면 신규
                success++;
            } catch (Exception e) {
                failed++;
                log.debug("NAVER_PLACE save fail id={} name='{}' reason={}",
                        r.getPlaceId(), r.getPlaceName(), e.getMessage());
            }
        }
        return new CrawlRes(picked, success, failed);
    }

    /* ---------------- upsert 공통 ---------------- */
    private boolean insertIfAbsent(LinkDoc doc) {
        Query q = Query.query(Criteria.where("_id").is(doc.getId()));
        Update u = new Update()
                .setOnInsert("_id", doc.getId())
                .setOnInsert("url", doc.getUrl())
                .setOnInsert("canonicalUrl", doc.getCanonicalUrl())
                .setOnInsert("platform", doc.getPlatform())
                .setOnInsert("keyword", doc.getKeyword())
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
