package com.likelion.cleopatra.domain.platform.naver.dto.blog;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NaverBlogItem {
    private String title;
    private String link;
    private String description;
    private String bloggername;
    private String bloggerlink;
    private String postdate;
}
