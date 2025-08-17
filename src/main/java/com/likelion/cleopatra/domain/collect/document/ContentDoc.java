package com.likelion.cleopatra.domain.collect.document;

import com.likelion.cleopatra.domain.crwal.dto.CrawlRes;
import com.likelion.cleopatra.domain.crwal.dto.place.NaverPlaceContentRes;
import com.likelion.cleopatra.global.common.enums.Platform;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndex(name="plat_time_idx", def="{ 'platform':1, 'crawledAt':-1 }")
@Document(collection = "contents")
public class ContentDoc {
    @Id private String id;              // LinkDoc.id와 동일(1:1)
    private Platform platform;
    private String url;
    private String canonicalUrl;
    private String title;
    private String contentHtml;
    private String contentText;
    private Instant crawledAt;

    public static ContentDoc fromBlog(LinkDoc link, CrawlRes.NaverBlogContentRes r) {
        return ContentDoc.builder()
                .id(link.getId())
                .platform(link.getPlatform())
                .url(link.getUrl())
                .canonicalUrl(link.getCanonicalUrl())
                .title(r.getTitle())
                .contentHtml(r.getHtml())
                .contentText(r.getText())
                .crawledAt(Instant.now())
                .build();
    }

    public static ContentDoc fromPlace(LinkDoc link, NaverPlaceContentRes res){
        String text = res.getReviews()==null? "" :
                res.getReviews().stream()
                        .map(rv -> rv.getBody()==null? "" : rv.getBody().trim())
                        .filter(s->!s.isBlank())
                        .collect(Collectors.joining("\n\n"));
        String json;
        try {
            json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(res);
        } catch (Exception e) { json = "{}"; }

        return ContentDoc.builder()
                .id(link.getId())
                .platform(link.getPlatform())
                .url(link.getUrl())
                .canonicalUrl(link.getCanonicalUrl())
                .title(link.getPlaceTitle())
                .contentHtml("")                 // 리뷰는 HTML 저장 안 함
                .contentText(text)
                //.reviewCount(res.getReviews()==null?0:res.getReviews().size())
                //.reviewJson(json)
                .crawledAt(Instant.now())
                .build();
    }
}