package com.likelion.cleopatra.domain.collect.document;

import com.likelion.cleopatra.domain.crwal.dto.CrawlRes;
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

    // ContentDoc.java (메서드 추가/구현)
    public static ContentDoc fromReview(LinkDoc link, String placeName, String reviewJson) {
        String title = "[리뷰] " + (placeName == null ? "" : placeName) + " 방문자 리뷰";
        return ContentDoc.builder()
                .id(link.getId())                        // LinkDoc와 1:1
                .platform(link.getPlatform())            // NAVER_PLACE 등
                .url(link.getUrl())
                .canonicalUrl(link.getCanonicalUrl())
                .title(title.trim())
                .contentHtml(null)                       // HTML 없음
                .contentText(reviewJson)                 // 리뷰 JSON payload
                .crawledAt(Instant.now())
                .build();
    }
}