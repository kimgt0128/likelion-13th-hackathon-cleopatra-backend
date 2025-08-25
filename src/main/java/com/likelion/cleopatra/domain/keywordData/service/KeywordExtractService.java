package com.likelion.cleopatra.domain.keywordData.service;

// 로그 찍기
import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion.cleopatra.domain.collect.repository.ContentRepository;
import com.likelion.cleopatra.domain.crwal.document.ContentDoc;
import com.likelion.cleopatra.domain.keywordData.document.KeywordDoc;
import com.likelion.cleopatra.domain.keywordData.dto.KeywordExtractReq;
import com.likelion.cleopatra.domain.keywordData.dto.KeywordExtractRes;
import com.likelion.cleopatra.domain.keywordData.dto.webClient.KeywordDescriptionReq;
import com.likelion.cleopatra.domain.keywordData.dto.webClient.KeywordDescriptionRes;
import com.likelion.cleopatra.domain.keywordData.dto.webClient.KeywordDescriptionRes.PlatformBlock;
import com.likelion.cleopatra.domain.keywordData.repository.KeywordRepository;
import com.likelion.cleopatra.global.common.enums.Platform;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class KeywordExtractService {

    private final ContentRepository contentRepository;
    private final KeywordRepository keywordRepository;
    private final WebClient webClient;
    private final ObjectMapper mapper;

    public KeywordExtractService(ContentRepository contentRepository,
                                 KeywordRepository keywordRepository,
                                 @Qualifier("keywordWebClient") WebClient webClient,
                                 ObjectMapper mapper) {
        this.contentRepository = contentRepository;
        this.keywordRepository = keywordRepository;
        this.webClient = webClient;
        this.mapper = mapper;
    }

    /** 행정구역+카테고리 기반 수집 → AI 요약 → 플랫폼별 문서 저장 → 요약 응답 */
    public KeywordExtractRes analyzeAndSave(KeywordExtractReq req) {
        final String category = req.getPrimary().getKo() + " " + req.getSecondary().getKo();

        // 1) 수집물 조회(최대 30)
        List<ContentDoc> blogs  = contentRepository.findTop30ByPlatformAndKeywordOrderByCrawledAtDesc(Platform.NAVER_BLOG,  category);
        List<ContentDoc> places = contentRepository.findTop30ByPlatformAndKeywordOrderByCrawledAtDesc(Platform.NAVER_PLACE, category);
        List<ContentDoc> yt     = contentRepository.findTop30ByPlatformAndKeywordOrderByCrawledAtDesc(Platform.YOUTUBE,    category);

        // [LOG-2] 리포지토리 조회 결과 전체 필드
        if (!blogs.isEmpty())  log.info("[KeywordExtract] repo NAVER_BLOG count={} docs={}", blogs.size(), asJson(blogs));
        if (!places.isEmpty()) log.info("[KeywordExtract] repo NAVER_PLACE count={} docs={}", places.size(), asJson(places));
        if (!yt.isEmpty())     log.info("[KeywordExtract] repo YOUTUBE count={} docs={}", yt.size(), asJson(yt));

        // 2) AI 요청 페이로드
        Map<String, List<KeywordDescriptionReq.Snippet>> data = new LinkedHashMap<>();
        data.put("data_naver_blog",  toSnippets(blogs));
        data.put("data_naver_palce", toSnippets(places)); // 사양 철자 유지
        // data.put("data_youtube",     toSnippets(yt));   // 필요 시 활성화

        KeywordDescriptionReq payload = KeywordDescriptionReq.of(req, data);

        // [LOG-1] AI 전송 직전 요청 DTO 전체 필드
        log.info("[KeywordExtract] -> AI request dto={}", asJson(payload));

        // 3) AI 호출 (base-url = http://ai:8000/api/ai)
        KeywordDescriptionRes ai = webClient.post()
                .uri("/analyze")
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(KeywordDescriptionRes.class)
                .block();

        // [LOG-3] AI 응답 DTO 전체 필드
        log.info("[KeywordExtract] <- AI response dto={}", asJson(ai));

        // 4) 응답 → 저장
        List<KeywordDoc> toSave = new ArrayList<>();
        if (ai != null && ai.getData() != null) {
            for (PlatformBlock b : ai.getData().values()) {
                Platform p = toPlatform(b.getPlatform());
                if (p == null) continue;

                KeywordDoc doc = new KeywordDoc(
                        category,
                        req.getDistrict(),
                        req.getNeighborhood(),
                        req.getPrimary(),
                        req.getSecondary(),
                        p,
                        b.getPlatform_keyword(),
                        b.getPlatform_description()
                );
                toSave.add(doc);
            }
        }
        List<KeywordDoc> saved = toSave.isEmpty() ? List.of() : keywordRepository.saveAll(toSave);

        // 5) 요약 응답
        String area = payload.getArea();
        return KeywordExtractRes.of(
                area,
                category,
                saved,
                sizeOf(blogs),
                sizeOf(places),
                sizeOf(yt)
        );
    }

    // --- helper
    private int sizeOf(List<?> xs) { return xs == null ? 0 : xs.size(); }

    private List<KeywordDescriptionReq.Snippet> toSnippets(List<ContentDoc> list) {
        if (list == null) return List.of();
        return list.stream()
                .map(c -> KeywordDescriptionReq.Snippet.builder()
                        .platform(c.getPlatform() == null ? null : c.getPlatform().name())
                        .text(c.getContentText())
                        .build())
                .collect(Collectors.toList());
    }

    private Platform toPlatform(String s) {
        if (s == null) return null;
        try { return Platform.valueOf(s.trim().toUpperCase()); }
        catch (Exception e) { return null; }
    }

    private String asJson(Object o) {
        try { return mapper.writeValueAsString(o); }
        catch (Exception e) { return String.valueOf(o); }
    }
}
