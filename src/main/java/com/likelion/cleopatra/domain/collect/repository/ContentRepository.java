package com.likelion.cleopatra.domain.collect.repository;

import com.likelion.cleopatra.domain.crwal.document.ContentDoc;
import com.likelion.cleopatra.global.common.enums.Platform;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ContentRepository extends MongoRepository<ContentDoc, String> {
    List<ContentDoc> findTop30ByPlatformAndKeywordOrderByCrawledAtDesc(Platform platform, String keyword);
}
