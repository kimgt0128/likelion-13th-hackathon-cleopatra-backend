package com.likelion.cleopatra.domain.data.repository;

import com.likelion.cleopatra.domain.data.document.LinkDoc;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LinksRepository extends MongoRepository<LinkDoc, String> {
    // 필요 시 커스텀 finder 추가 가능
}
