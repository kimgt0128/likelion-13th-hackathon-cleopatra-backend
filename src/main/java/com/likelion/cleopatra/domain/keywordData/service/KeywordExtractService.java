package com.likelion.cleopatra.domain.keywordData.service;

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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class KeywordExtractService {

    private final ContentRepository contentRepository;
    private final KeywordRepository keywordRepository;
    private final WebClient webClient;

    public KeywordExtractService(ContentRepository contentRepository,
                                 KeywordRepository keywordRepository,
                                 @Qualifier("keywordWebClient") WebClient webClient) {
        this.contentRepository = contentRepository;
        this.keywordRepository = keywordRepository;
        this.webClient = webClient;
    }

    /** 행정구역+카테고리 기반으로 수집 → AI 요약 → 플랫폼별 문서 저장 → 요약 응답 */
    public KeywordExtractRes analyzeAndSave(KeywordExtractReq req) {
        final String area   = req.getDistrict().getKo() + " " + req.getNeighborhood().getKo();
        final String query  = req.getPrimary().getKo() + " " + req.getSecondary().getKo();

        // 1) 수집물 조회(최대 30)
        List<ContentDoc> blogs  = contentRepository.findTop30ByPlatformAndKeywordOrderByCrawledAtDesc(Platform.NAVER_BLOG,  query);
        List<ContentDoc> places = contentRepository.findTop30ByPlatformAndKeywordOrderByCrawledAtDesc(Platform.NAVER_PLACE, query);
        List<ContentDoc> yt     = contentRepository.findTop30ByPlatformAndKeywordOrderByCrawledAtDesc(Platform.YOUTUBE,    query);

        // 2) AI 요청 페이로드
        Map<String, List<KeywordDescriptionReq.Snippet>> data = new LinkedHashMap<>();
        data.put("data_naver_blog",  toSnippets(blogs));
        data.put("data_naver_palce", toSnippets(places)); // 사양 철자 유지
        data.put("data_youtube",     toSnippets(yt));

        KeywordDescriptionReq payload = KeywordDescriptionReq.builder()
                .areaa(area)                // 사양 고정
                .keyword(query)
                .data(data)
                .build();

        // 3) AI 호출
        KeywordDescriptionRes ai = webClient.post()
                .uri("/alalyze")            // 사양 고정
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(KeywordDescriptionRes.class)
                .block();

        // 4) 응답 → 플랫폼별 KeywordDoc 생성 및 저장
        List<KeywordDoc> toSave = new ArrayList<>();
        if (ai != null && ai.getData() != null) {
            for (PlatformBlock b : ai.getData().values()) {
                Platform p = toPlatform(b.getPlatform());
                if (p == null) continue;
                KeywordDoc doc = KeywordDoc.builder()
                        .keyword(query)
                        .district(req.getDistrict())
                        .neighborhood(req.getNeighborhood())
                        .primary(req.getPrimary())
                        .secondary(req.getSecondary())
                        .platform(p)
                        .keywords(b.getPlatform_keyword())
                        .descript(b.getPlatform_description())
                        .build();
                toSave.add(doc);
            }
        }
        List<KeywordDoc> saved = toSave.isEmpty() ? List.of() : keywordRepository.saveAll(toSave);

        // 5) 요약 응답
        return KeywordExtractRes.of(
                area,
                query,
                saved,
                sizeOf(blogs),
                sizeOf(places),
                sizeOf(yt)
        );
    }

    private int sizeOf(List<?> xs) { return xs == null ? 0 : xs.size(); }

    private List<KeywordDescriptionReq.Snippet> toSnippets(List<ContentDoc> list) {
        if (list == null) return List.of();
        return list.stream()
                .map(c -> KeywordDescriptionReq.Snippet.builder()
                        .doc_id(c.getId())
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
}
