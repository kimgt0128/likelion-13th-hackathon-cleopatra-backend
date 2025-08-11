package com.likelion.cleopatra.global.config;

import com.likelion.cleopatra.domain.data.document.PingDoc;
import com.likelion.cleopatra.domain.data.repository.MongoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class MongoSmokeRunner {

    @Bean
    CommandLineRunner mongoSmokeTest(MongoRepository repo) {
        return args -> {
            // 1) 저장
            PingDoc saved = repo.save(PingDoc.builder()
                    .message("mongo ok")
                    .at(Instant.now())
                    .build());

            // 2) 카운트 + 하나 읽기
            long count = repo.count();
            log.info("[Mongo Smoke] saved id={}, total count={}", saved.getId(), count);

            repo.findById(saved.getId())
                    .ifPresent(doc -> log.info("[Mongo Smoke] loaded: msg={}, at={}", doc.getMessage(), doc.getAt()));
        };
    }
}