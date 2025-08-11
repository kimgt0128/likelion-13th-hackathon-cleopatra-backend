package com.likelion.cleopatra.domain.data.service;

import com.likelion.cleopatra.domain.data.document.LinkDoc;
import com.likelion.cleopatra.domain.data.dto.requeset.CollectNaverBlogReq;
import com.likelion.cleopatra.domain.data.dto.response.CollectResultRes;
import com.likelion.cleopatra.domain.data.repository.LinksRepository;
import com.likelion.cleopatra.domain.platform.naver.dto.blog.NaverBlogSearchRes;
import com.likelion.cleopatra.domain.platform.naver.service.NaverApiService;
import com.mongodb.client.result.UpdateResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class LinkCollectorService {

    private final NaverApiService naverApiService;
    private final LinksRepository linksRepository;
    private final MongoTemplate mongoTemplate;

    /**
     * 네이버 블로그 링크 수집/적재.
     * @param req 수집 요청
     * @return CollectResultRes (inserted, query, display, start, elapsedMs)
     */

    public CollectResultRes collectNaverBlogLinks(CollectNaverBlogReq req) {
        long t0 = System.currentTimeMillis();

        int display = Math.min(100, Optional.ofNullable(req.display()).orElse(50));
        int start   = Optional.ofNullable(req.start()).orElse(1);

        // 검색어: "행정동(한글) + 공백 + 2차 카테고리(한글)"
        String query = req.neighborhood().getKo() + " " + req.secondary().getKo();

        NaverBlogSearchRes res = naverApiService.searchBlog(query, display, start).block();

        int inserted = 0;
        if (res != null && res.getItems() != null) {
            inserted = (int) res.getItems().stream()
                    .map(item -> LinkDoc.fromNaver(
                            item.getLink(),
                            query,
                            req.primary(),      // 한글 그대로 저장 (ex. "요식업")
                            req.secondary(),    // 한글 그대로 저장 (ex. "치킨")
                            req.district(),     // Enum (DB에는 코드로)
                            req.neighborhood()  // Enum (DB에는 코드로)
                    ))
                    .filter(this::insertIfAbsent)
                    .count();
            log.info("Naver blog collected: query='{}' inserted={}, totalBatch={}",
                    query, inserted, res.getItems().size());
        } else {
            log.info("Naver blog collected: query='{}' inserted=0, totalBatch=0 (empty response)", query);
        }

        return CollectResultRes.builder()
                .inserted(inserted)
                .query(query)
                .display(display)
                .start(start)
                .elapsedMs(System.currentTimeMillis() - t0)
                .build();
    }


    /**
     * 존재 확인 없이 {@code _id} 기준으로 upsert를 수행합니다.
     * 모든 필드는 {@code $setOnInsert}로만 지정되어 기존 문서를 수정하지 않습니다.
     *
     * @param doc 삽입 대상 링크 문서
     * @return {@code true}: 신규 삽입됨, {@code false}: 이미 존재해 아무 작업도 수행하지 않음
     */
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
                .setOnInsert("status", doc.getStatus())
                .setOnInsert("priority", doc.getPriority())
                .setOnInsert("tries", doc.getTries())
                .setOnInsert("discoveredAt", doc.getDiscoveredAt())
                .setOnInsert("updatedAt", doc.getUpdatedAt());

        UpdateResult r = mongoTemplate.upsert(q, u, LinkDoc.class);
        return r.getUpsertedId() != null; // 새로 들어갔을 때만 true
    }
}