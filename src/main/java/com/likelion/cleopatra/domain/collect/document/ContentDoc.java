package com.likelion.cleopatra.domain.collect.document;

import com.likelion.cleopatra.domain.crwal.dto.NaverBlogCrawlRes;
import com.likelion.cleopatra.global.common.enums.Platform;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor
@Document(collection = "contents")
@CompoundIndex(name="plat_time_idx", def="{ 'platform':1, 'crawledAt':-1 }")
public class ContentDoc {
    @Id private String id;              // LinkDoc.id와 동일(1:1)
    private Platform platform;
    private String url;
    private String canonicalUrl;
    private String title;
    private String contentHtml;
    private String contentText;
    private Instant crawledAt;

    public static ContentDoc from(LinkDoc link, NaverBlogCrawlRes r) {
        return ContentDoc.builder()
                .id(link.getId())
                .platform(link.getPlatform())
                .url(link.getUrl())
                .canonicalUrl(link.getCanonicalUrl())
                .title(r.title())
                .contentHtml(r.html())
                .contentText(r.text())
                .crawledAt(Instant.now())
                .build();
    }
}