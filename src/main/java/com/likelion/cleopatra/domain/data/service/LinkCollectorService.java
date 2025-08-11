package com.likelion.cleopatra.domain.data.service;

import com.likelion.cleopatra.domain.data.document.LinkDoc;
import com.likelion.cleopatra.domain.data.dto.CollectNaverBlogReq;
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
     * 네이버 블로그 검색 API를 호출해 링크를 수집/적재합니다.
     *
     * @param req 수집 요청 DTO
     * @return 이번 호출로 새로 삽입된 링크 수(이미 존재한 링크는 카운트하지 않음)
     */

    public int collectNaverBlogLinks(CollectNaverBlogReq req) {
        int display = Math.min(100, Optional.ofNullable(req.display()).orElse(50));
        int start   = Optional.ofNullable(req.start()).orElse(1);

        // 검색어: "행정동 + 공백 + 2차 카테고리"(String + Enum = String + Enum.toSring() 호출)
        String query = req.neighborhood().getKo() + " " + req.secondary();

        NaverBlogSearchRes res = naverApiService.searchBlog(query, display, start).block();

        if (res == null || res.getItems() == null) return 0;

        int inserted = (int) res.getItems().stream()
                .map(item -> LinkDoc.fromNaver(
                        item.getLink(),
                        query, req.primary(),
                        req.secondary(),
                        req.district(),
                        req.neighborhood()
                ))
                .filter(this::insertIfAbsent)
                .count();
        log.info("Naver blog collected: query='{}' inserted={}, totalBatch={}",
                query, inserted, res.getItems().size());

        return inserted;
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