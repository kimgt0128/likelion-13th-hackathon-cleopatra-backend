package com.likelion.cleopatra.domain.keywordData.service;

import com.likelion.cleopatra.domain.collect.repository.ContentRepository;
import com.likelion.cleopatra.domain.crwal.document.ContentDoc;
import com.likelion.cleopatra.domain.keywordData.document.KeywordDoc;
import com.likelion.cleopatra.domain.keywordData.dto.KeywordExtractRes;
import com.likelion.cleopatra.domain.keywordData.dto.webClient.KeywordDescriptionReq;
import com.likelion.cleopatra.domain.keywordData.dto.webClient.KeywordDescriptionRes;
import com.likelion.cleopatra.domain.keywordData.repository.KeywordRepository;
import com.likelion.cleopatra.global.common.enums.Platform;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class KeywordService {

    private final ContentRepository contentRepository;
    private final KeywordRepository keywordRepository;
    private final WebClient webClient; // @Qualifier 필요

    public KeywordService(ContentRepository contentRepository,
                          KeywordRepository keywordRepository,
                          @Qualifier("keywordWebClient") WebClient webClient) {
        this.contentRepository = contentRepository;
        this.keywordRepository = keywordRepository;
        this.webClient = webClient;
    }
    /**
     * area + query로 콘텐츠를 모아 AI에 전달하고 결과를 저장.
     */
    public KeywordExtractRes analyzeAndSave(String area, String query) {
        // 1) 수집물 모으기(최대 30개씩)
        List<ContentDoc> blogs  = contentRepository.findTop30ByPlatformAndKeywordOrderByCrawledAtDesc(Platform.NAVER_BLOG,  query);
        List<ContentDoc> places = contentRepository.findTop30ByPlatformAndKeywordOrderByCrawledAtDesc(Platform.NAVER_PLACE, query);
        List<ContentDoc> yt     = contentRepository.findTop30ByPlatformAndKeywordOrderByCrawledAtDesc(Platform.YOUTUBE,    query);

        // 2) 요청 DTO 구성 (키 이름은 요구 사양 유지)
        Map<String, List<KeywordDescriptionReq.Snippet>> data = new LinkedHashMap<>();
        data.put("data_naver_blog",  toSnippets(blogs));
        data.put("data_naver_palce", toSnippets(places)); // 요구 사양 철자 유지
        data.put("data_youtube",     toSnippets(yt));

        KeywordDescriptionReq req = KeywordDescriptionReq.builder()
                .areaa(area)        // 요구 사양 필드명 유지
                .keyword(query)
                .data(data)
                .build();

        // 3) AI 호출 (POST /alalyze)
        KeywordDescriptionRes res = webClient.post()
                .uri("/alalyze")
                .bodyValue(req)
                .retrieve()
                .bodyToMono(KeywordDescriptionRes.class)
                .block();

        // 4) 응답 → KeywordDoc 저장
        KeywordDoc doc = mapToDoc(query, res);
        keywordRepository.save(doc);

        return KeywordExtractRes.of(
                area, query, doc,
                blogs == null ? 0 : blogs.size(),
                places == null ? 0 : places.size(),
                yt == null ? 0 : yt.size()
        );
    }

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

    private KeywordDoc mapToDoc(String query, KeywordDescriptionRes res) {
        List<KeywordDoc.PlatformKeywords> items = new ArrayList<>();
        if (res != null && res.getData() != null) {
            for (KeywordDescriptionRes.PlatformBlock b : res.getData().values()) {
                Platform p = safePlatform(b.getPlatform());
                if (p == null) continue;
                items.add(KeywordDoc.PlatformKeywords.builder()
                        .platform(p)
                        .keywords(b.getPlatform_keyword())
                        .descript(b.getPlatform_description())
                        .build());
            }
        }
        return KeywordDoc.builder()
                .keyword(query)
                .keywords(items)
                .build();
    }

    private Platform safePlatform(String s) {
        if (s == null) return null;
        try { return Platform.valueOf(s.trim().toUpperCase()); }
        catch (Exception e) { return null; }
    }
}
