package com.likelion.cleopatra.domain.collect.repository;

import com.likelion.cleopatra.domain.collect.document.LinkDoc;
import com.likelion.cleopatra.domain.collect.document.LinkStatus;
import com.likelion.cleopatra.global.common.enums.Platform;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * 크롤링 처리 성능
 * - Pageable + Sort는 Mongo 서버측 정렬/제한으로 푸시다운된다.
 * - 성능 전제: 복합 인덱스 { platform:1, status:1, priority:-1, updatedAt:1 } 필수.
 * - explain로 확인: SORT 단계가 없어야 한다. (blocking sort 금지)
 */
public interface LinkDocRepository extends MongoRepository<LinkDoc, String> {
    /**
     * 배치 페치 쿼리
     * query : platform=?, status=?
     * sort  : priority DESC, updatedAt ASC
     * limit : page.size (예: 50)
     *
     * 주의:
     * - 정렬 방향이 인덱스와 다르면 인덱스 커버 정렬이 깨진다.
     * - page 번호는 항상 0으로 고정. “다음 배치”는 updatedAt/status 변경으로 자연히 분리된다.
     */
    Page<LinkDoc> findByPlatformAndStatus(Platform platform, LinkStatus status, Pageable pageable);
    Page<LinkDoc> findByPlatformAndStatusAndKeyword(
            Platform platform,
            LinkStatus status,
            String keyword,
            Pageable pageable
    );
}
