package com.likelion.cleopatra.domain.openApi.naver.dto;

import com.likelion.cleopatra.domain.openApi.naver.dto.blog.NaverBlogItem;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class NaverSearchRes<T> {
    private String lastBuildDate;
    private int total;
    private int start;
    private int display;
    private List<T> items;
}
