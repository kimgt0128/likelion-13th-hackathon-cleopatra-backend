package com.likelion.cleopatra.domain.crwal.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion.cleopatra.domain.collect.document.ContentDoc;
import com.likelion.cleopatra.domain.collect.document.LinkDoc;
import com.likelion.cleopatra.domain.collect.document.LinkStatus;
import com.likelion.cleopatra.domain.collect.repository.ContentRepository;
import com.likelion.cleopatra.domain.collect.repository.LinkDocRepository;
import com.likelion.cleopatra.domain.crwal.dto.CrawlRes;
import com.likelion.cleopatra.domain.crwal.dto.place.NaverPlaceLinkRes;
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
import java.util.List;

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

    public CrawlRes naverPlaceCrawl(String keyword, int places) {
        int placeLimit = Math.max(1, Math.min(50, places));

        List<NaverPlaceLinkRes> list = naverPlaceCrawler.crawlLinks(keyword, placeLimit);
        int picked = (list == null) ? 0 : list.size();
        int success = 0, failed = 0;

        if (picked == 0) {
            log.info("NAVER_PLACE skip keyword='{}' reason=empty-result", keyword);
            return new CrawlRes(0, 0, 0);
        }

        for (NaverPlaceLinkRes r : list) {
            try {
                // 저장 직전 로그
                log.debug("NAVER_PLACE before-save id={} name='{}' url={}",
                        r.getPlaceId(), r.getPlaceName(), r.getPlaceUrl());

                LinkDoc doc = LinkDoc.fromNaverPlaceLink(
                        r.getPlaceId(),
                        r.getPlaceName(),
                        r.getPlaceUrl(), // https://map.naver.com/p/search/{query}/place/{placeId}
                        keyword,
                        null, null, null, null // 필요 시 도메인 값 주입
                );

                linkDocRepository.save(doc); // HTML 미저장, 링크 메타만
                success++;
            } catch (Exception e) {
                failed++;
                log.debug("NAVER_PLACE save=FAIL id={} name='{}' reason={}",
                        r.getPlaceId(), r.getPlaceName(), e.toString());
            }
        }

        log.info("NAVER_PLACE summary keyword='{}' picked={} success={} failed={}",
                keyword, picked, success, failed);

        return new CrawlRes(picked, success, failed);
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
