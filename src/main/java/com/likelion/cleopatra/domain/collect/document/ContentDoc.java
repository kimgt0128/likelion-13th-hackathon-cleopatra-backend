package com.likelion.cleopatra.domain.collect.document;

import com.likelion.cleopatra.domain.crwal.dto.CrawlRes;
import com.likelion.cleopatra.global.common.enums.Platform;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@CompoundIndex(name="plat_time_idx", def="{ 'platform':1, 'crawledAt':-1 }")
@Document(collection = "contents")
public class ContentDoc {
    @Id private String id;              // LinkDoc.id와 동일(1:1)
    private Platform platform;
    private String keyword;
    private String url;
    private String canonicalUrl;
    private String title; // 블로그 제목, 네이버 플레이스 가게 이름, 유튜브 영상 제목
    private String contentHtml;
    private String contentText;
    private Instant crawledAt;

    public static ContentDoc fromBlog(LinkDoc link, CrawlRes.NaverBlogContentRes r) {
        return ContentDoc.builder()
                .id(link.getId())
                .platform(link.getPlatform())
                .url(link.getUrl())
                .canonicalUrl(link.getCanonicalUrl())
                .keyword(link.getKeyword())
                .title(r.getTitle())
                .contentHtml(r.getHtml())
                .contentText(r.getText())
                .crawledAt(Instant.now())
                .build();
    }

    // ContentDoc.java (메서드 추가/구현)
    public static ContentDoc fromReview(LinkDoc link, String placeName, String reviewJson) {
        return ContentDoc.builder()
                .id(link.getId())
                .platform(link.getPlatform())
                .url(link.getUrl())
                .canonicalUrl(link.getCanonicalUrl())
                .keyword(link.getKeyword())
                .title(placeName)                 // 플레이스 이름
                .contentHtml(null)                // 리뷰는 텍스트(JSON)로 저장
                .contentText(reviewJson)          // 리뷰 배열 JSON
                .crawledAt(Instant.now())
                .build();
    }
}