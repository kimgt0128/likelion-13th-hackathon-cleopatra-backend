package com.likelion.cleopatra.domain.data.repository;

import com.likelion.cleopatra.domain.data.document.PingDoc;

public interface MongoRepository extends org.springframework.data.mongodb.repository.MongoRepository<PingDoc, String> {
}
