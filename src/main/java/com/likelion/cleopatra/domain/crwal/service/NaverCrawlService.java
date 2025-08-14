package com.likelion.cleopatra.domain.crwal.service;

import com.likelion.cleopatra.domain.collect.document.ContentDoc;
import com.likelion.cleopatra.domain.collect.document.LinkDoc;
import com.likelion.cleopatra.domain.collect.document.LinkStatus;
import com.likelion.cleopatra.domain.collect.repository.ContentRepository;
import com.likelion.cleopatra.domain.collect.repository.LinkDocRepository;
import com.likelion.cleopatra.domain.crwal.dto.CrawlRes;
import com.likelion.cleopatra.domain.crwal.exception.CrawlErrorCode;
import com.likelion.cleopatra.domain.crwal.exception.CrawlException;
import com.likelion.cleopatra.domain.crwal.exception.failure.FailureClassifier;
import com.likelion.cleopatra.domain.crwal.impl.NaverBlogCrawler;
import com.likelion.cleopatra.global.common.enums.Platform;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;



@Slf4j
@RequiredArgsConstructor
@Service
public class NaverCrawlService {

    private final LinkDocRepository linkDocRepository;
    private final ContentRepository contentRepository;
    private final NaverBlogCrawler naverBlogCrawler;
    private final FailureClassifier failureClassifier;
    // 필요 시 동시 처리: @Qualifier("crawlerExecutor") ThreadPoolTaskExecutor exec; 현재 순차로 충분

    /**
     * CrawlRes
     * - 정렬/제한은 DB가 수행. 애플리케이션 메모리 정렬 없음.
     * - sort 키와 인덱스(plat_stat_pri_upd) 일치가 핵심.
     * - 배치 크기(50)는 네트워크/CPU 절충값. QPS에 맞춰 조정.
     * - 상태머신: NEW → FETCHING → FETCHED|FAILED
     * - 단일 인스턴스 전제. 멀티 인스턴스면 findAndModify로 “원자적 클레임” 패턴을 쓴다.
     */

    // @Scheduled(fixedDelay = 60_000)
    public CrawlRes naverBlogCrawl(int size) {
        var sort = Sort.by(Sort.Order.desc("priority"), Sort.Order.asc("updatedAt"));
        var page = PageRequest.of(0, size, sort);

        var batch = linkDocRepository.findByPlatformAndStatus(Platform.NAVER_BLOG, LinkStatus.NEW, page).getContent();
        if (batch.isEmpty()) {
            log.info("CRAWL skip platform=NAVER_BLOG reason=empty-batch");
            throw new CrawlException(CrawlErrorCode.NO_LINKS_TO_CRAWL);
        }

        // FETCHING 마킹
        var now = Instant.now();
        batch.forEach(d -> d.markFetching(now));
        linkDocRepository.saveAll(batch);

        // 최소 구현: 순차 처리(해커톤용). 이후 필요시 exec.submit(processOneSafe)로 교체.
        int success = 0, fail = 0;
        for (var doc : batch) {
            if(processOneSafe(doc)) success++; else fail++;
        }
        return new CrawlRes(batch.size(), success, fail);
    }

    private boolean processOneSafe(LinkDoc doc) {
        final String url = doc.getUrl();
        try {
            var res = naverBlogCrawler.crawl(url);                 // 실패 시 예외
            contentRepository.save(ContentDoc.from(doc, res));  // contents 컬렉션 저장

            // 성공 마킹
            doc.markFetched(Instant.now());
            linkDocRepository.save(doc);

            // 쿼리 확인
            log.info("CRAWL ok platform=NAVER_BLOG url={} textLen={}", url, len(res.getText()));
            log.debug("CRAWL detail url={} title='{}' htmlLen={} textLen={} sample=\"{}\"",
                    url, compact(res.getTitle()), len(res.getHtml()), len(res.getText()), sample(res.getText(), 160));
            return true;

        } catch (Exception e) {

            // 실패시 처리
            var err = failureClassifier.classify(e);
            doc.markFailed(Instant.now(), err.message());   // DB엔 메시지만 저장
            linkDocRepository.save(doc);

            log.info("CRAWL fail platform=NAVER_BLOG url={} reason={}", url, e.getClass().getSimpleName());
            log.debug("CRAWL fail detail url={} msg={}", url, e.getMessage());
            return false;
        }
    }

    // 유틸
    private int len(String s){ return s==null?0:s.length(); }
    private String compact(String s){ return s==null?"":s.replaceAll("\\s+"," ").trim(); }
    private String sample(String s,int n){ if(s==null)return""; var t=compact(s); return t.substring(0, Math.min(t.length(), n)); }
}