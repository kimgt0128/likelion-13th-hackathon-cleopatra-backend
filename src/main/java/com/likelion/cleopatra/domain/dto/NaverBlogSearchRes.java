package com.likelion.cleopatra.domain.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NaverBlogSearchRes {
    private String lastBuildDate;
    private int total;
    private int start;
    private int display;
    private List<NaverBlogItem> items;
}
