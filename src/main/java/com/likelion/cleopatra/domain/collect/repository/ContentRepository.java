package com.likelion.cleopatra.domain.collect.repository;

import com.likelion.cleopatra.domain.collect.document.ContentDoc;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ContentRepository extends MongoRepository<ContentDoc, String> {}
