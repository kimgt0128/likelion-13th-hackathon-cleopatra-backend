package com.likelion.cleopatra.domain.keywordData.repository;

import com.likelion.cleopatra.domain.keywordData.document.KeywordDoc;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface KeywordRepository extends MongoRepository<KeywordDoc, String> {
    Optional<KeywordDoc> findTopByKeywordOrderByIdDesc(String keyword);
}