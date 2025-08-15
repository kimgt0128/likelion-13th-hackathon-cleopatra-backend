package com.likelion.cleopatra.domain.crwal.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CrawlRes {
    private int picked;
    private int success;
    private int failed;
}
