package com.likelion.cleopatra.domain.collect.repository;

import com.likelion.cleopatra.domain.collect.document.LinkDoc;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LinksRepository extends MongoRepository<LinkDoc, String> {
    // 필요 시 커스텀 finder 추가 가능
}
