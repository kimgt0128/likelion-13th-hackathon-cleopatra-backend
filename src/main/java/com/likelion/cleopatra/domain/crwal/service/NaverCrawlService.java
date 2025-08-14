package com.likelion.cleopatra.domain.crwal.service;

import com.likelion.cleopatra.domain.collect.document.ContentDoc;
import com.likelion.cleopatra.domain.collect.document.LinkDoc;
import com.likelion.cleopatra.domain.collect.document.LinkStatus;
import com.likelion.cleopatra.domain.collect.repository.ContentRepository;
import com.likelion.cleopatra.domain.collect.repository.LinkDocRepository;
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
    // 필요 시 동시 처리: @Qualifier("crawlerExecutor") ThreadPoolTaskExecutor exec; 현재 순차로 충분

    /**
     * 실무 메모
     * - 정렬/제한은 DB가 수행. 애플리케이션 메모리 정렬 없음.
     * - sort 키와 인덱스(plat_stat_pri_upd) 일치가 핵심.
     * - 배치 크기(50)는 네트워크/CPU 절충값. QPS에 맞춰 조정.
     * - 상태머신: NEW → FETCHING → FETCHED|FAILED
     * - 단일 인스턴스 전제. 멀티 인스턴스면 findAndModify로 “원자적 클레임” 패턴을 쓴다.
     */

    // @Scheduled(fixedDelay = 60_000)
    public void naverBlogCrawl() {
        var sort = Sort.by(Sort.Order.desc("priority"), Sort.Order.asc("updatedAt"));
        var page = PageRequest.of(0, 50, sort);

        var batch = linkDocRepository.findByPlatformAndStatus(Platform.NAVER_BLOG, LinkStatus.NEW, page).getContent();
        if (batch.isEmpty()) {
            log.info("CRAWL skip platform=NAVER_BLOG reason=empty-batch");
            return;
        }

        // FETCHING 마킹
        var now = Instant.now();
        batch.forEach(d -> d.markFetching(now));
        linkDocRepository.saveAll(batch);

        // 최소 구현: 순차 처리(해커톤용). 필요하면 exec.submit(processOneSafe)로 교체.
        for (var doc : batch) processOneSafe(doc);
    }

    private void processOneSafe(LinkDoc doc) {
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

        } catch (Exception e) {

            // 실패시 처리
            doc.markFailed(Instant.now(), e.getClass().getSimpleName() + ":" + e.getMessage());
            linkDocRepository.save(doc);

            log.info("CRAWL fail platform=NAVER_BLOG url={} reason={}", url, e.getClass().getSimpleName());
            log.debug("CRAWL fail detail url={} msg={}", url, e.getMessage());
        }
    }

    // 유틸
    private int len(String s){ return s==null?0:s.length(); }
    private String compact(String s){ return s==null?"":s.replaceAll("\\s+"," ").trim(); }
    private String sample(String s,int n){ if(s==null)return""; var t=compact(s); return t.substring(0, Math.min(t.length(), n)); }
}