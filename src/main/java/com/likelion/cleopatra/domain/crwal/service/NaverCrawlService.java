package com.likelion.cleopatra.domain.crwal.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion.cleopatra.domain.collect.document.ContentDoc;
import com.likelion.cleopatra.domain.collect.document.LinkDoc;
import com.likelion.cleopatra.domain.collect.document.LinkStatus;
import com.likelion.cleopatra.domain.collect.repository.ContentRepository;
import com.likelion.cleopatra.domain.collect.repository.LinkDocRepository;
import com.likelion.cleopatra.domain.crwal.dto.CrawlRes;
import com.likelion.cleopatra.domain.crwal.dto.place.NaverPlaceContentRes;
import com.likelion.cleopatra.domain.crwal.dto.place.NaverPlaceReview;
import com.likelion.cleopatra.domain.crwal.exception.CrawlErrorCode;
import com.likelion.cleopatra.domain.crwal.exception.CrawlException;
import com.likelion.cleopatra.domain.crwal.exception.failure.FailureClassifier;
import com.likelion.cleopatra.domain.crwal.impl.NaverBlogCrawler;
import com.likelion.cleopatra.domain.crwal.impl.NaverPlaceCrawler;
import com.likelion.cleopatra.global.common.enums.Platform;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class NaverCrawlService {

    private final LinkDocRepository linkDocRepository;
    private final ContentRepository contentRepository;
    private final NaverBlogCrawler naverBlogCrawler;
    private final NaverPlaceCrawler naverPlaceCrawler;
    private final FailureClassifier failureClassifier;
    private final ObjectMapper om = new ObjectMapper();

    // @Scheduled(fixedDelay = 60_000)
    public CrawlRes naverBlogCrawl(int size) {
        var sort = Sort.by(Sort.Order.desc("priority"), Sort.Order.asc("updatedAt"));
        var page = PageRequest.of(0, size, sort);

        var batch = linkDocRepository.findByPlatformAndStatus(Platform.NAVER_BLOG, LinkStatus.NEW, page).getContent();
        if (batch.isEmpty()) {
            log.info("CRAWL skip platform=NAVER_BLOG reason=empty-batch");
            throw new CrawlException(CrawlErrorCode.NO_LINKS_TO_CRAWL);
        }

        var now = Instant.now();
        batch.forEach(d -> d.markFetching(now));
        linkDocRepository.saveAll(batch);

        int success = 0, fail = 0;
        for (var doc : batch) {
            if (processOneSafe(doc)) success++; else fail++;
        }
        return new CrawlRes(batch.size(), success, fail);
    }

    /** 키워드로 플레이스 리뷰 수집 후 ContentDoc 저장. reviewJson에는 리뷰 배열만 저장 */
    public CrawlRes naverPlaceCrawl(String keyword, int places, int per) {
        int placeLimit = Math.max(1, Math.min(20, places));
        int perReview  = Math.max(1, Math.min(20, per));

        List<NaverPlaceContentRes> list = naverPlaceCrawler.crawl(keyword, placeLimit, perReview);

        int picked = placeLimit;
        int succeededPlaces = list.size();
        int failedPlaces = Math.max(0, picked - succeededPlaces);

        int reviewTotal = 0;

        for (NaverPlaceContentRes r : list) {
            // 리뷰 배열만(JSON): [{placeId, visitKeywords, body, revisit, tags}, ...]
            List<ReviewJson> reviewsForJson = new ArrayList<>();
            if (r.getReviews() != null) {
                for (NaverPlaceReview rv : r.getReviews()) {
                    reviewsForJson.add(new ReviewJson(
                            r.getPlaceId(),
                            rv.getVisitKeywords(),
                            rv.getBody(),
                            rv.getRevisit(),
                            rv.getTags()
                    ));
                }
            }
            reviewTotal += reviewsForJson.size();

            String text = reviewsForJson.stream()
                    .map(x -> x.body == null ? "" : x.body.trim())
                    .filter(s -> !s.isBlank())
                    .collect(Collectors.joining("\n\n"));

            String json;
            try { json = om.writeValueAsString(reviewsForJson); }
            catch (Exception e) { json = "[]"; }

            ContentDoc doc = ContentDoc.builder()
                    .platform(Platform.NAVER_PLACE)
                    .url(r.getPlaceUrl())
                    .canonicalUrl(null)
                    .title(r.getPlaceName())
                    .contentHtml("")                   // HTML 저장 안 함
                    .contentText(text)
                    //.reviewCount(reviewsForJson.size())
                    //.reviewJson(json)                  // 리뷰 배열 JSON만 저장
                    .crawledAt(Instant.now())
                    .build();

            contentRepository.save(doc);
        }

        return new CrawlRes(picked, reviewTotal, failedPlaces);
    }

    private boolean processOneSafe(LinkDoc doc) {
        final String url = doc.getUrl();
        try {
            var res = naverBlogCrawler.crawl(url);
            contentRepository.save(ContentDoc.fromBlog(doc, res));

            doc.markFetched(Instant.now());
            linkDocRepository.save(doc);

            log.info("CRAWL ok platform=NAVER_BLOG url={} textLen={}", url, len(res.getText()));
            log.debug("CRAWL detail url={} title='{}' htmlLen={} textLen={} sample=\"{}\"",
                    url, compact(res.getTitle()), len(res.getHtml()), len(res.getText()), sample(res.getText(), 160));
            return true;

        } catch (Exception e) {
            var err = failureClassifier.classify(e);
            doc.markFailed(Instant.now(), err.message());
            linkDocRepository.save(doc);

            log.info("CRAWL fail platform=NAVER_BLOG url={} reason={}", url, e.getClass().getSimpleName());
            log.debug("CRAWL fail detail url={} msg={}", url, e.getMessage());
            return false;
        }
    }

    // util
    private int len(String s){ return s==null?0:s.length(); }
    private String compact(String s){ return s==null?"":s.replaceAll("\\s+"," ").trim(); }
    private String sample(String s,int n){ if(s==null)return""; var t=compact(s); return t.substring(0, Math.min(t.length(), n)); }

    /** reviewJson 직렬화를 위한 경량 구조체 */
    private static final class ReviewJson {
        public final String placeId;
        public final List<String> visitKeywords;
        public final String body;
        public final String revisit;
        public final List<String> tags;

        ReviewJson(String placeId, List<String> visitKeywords, String body, String revisit, List<String> tags) {
            this.placeId = placeId;
            this.visitKeywords = visitKeywords;
            this.body = body;
            this.revisit = revisit;
            this.tags = tags;
        }
    }
}
