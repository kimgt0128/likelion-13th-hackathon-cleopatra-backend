// src/main/java/com/likelion/cleopatra/global/config/crawler/PlayWrightConfig.java
package com.likelion.cleopatra.global.config.crawler;

import com.microsoft.playwright.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Map;

@Configuration
public class PlayWrightConfig {

    @Bean(destroyMethod = "close")
    public Playwright playwright() {
        return Playwright.create();
    }

    @Bean(destroyMethod = "close")
    public Browser browser(Playwright playwright) {
        // -Dplaywright.headless=false 로 로컬 디버깅
        boolean headless = Boolean.parseBoolean(System.getProperty("playwright.headless", "true"));

        return playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(headless)
                .setArgs(List.of(
                        "--disable-blink-features=AutomationControlled",
                        "--no-first-run",
                        "--no-default-browser-check",
                        "--disable-dev-shm-usage",
                        "--disable-gpu"
                )));
    }

    @Bean(destroyMethod = "close")
    public BrowserContext browserContext(Browser browser) {
        return browser.newContext(new Browser.NewContextOptions()
                // 데스크톱 강제
                .setIsMobile(false)
                .setViewportSize(1440, 900)
                .setDeviceScaleFactor(1)
                .setLocale("ko-KR")
                .setTimezoneId("Asia/Seoul")
                .setUserAgent(
                        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                                "AppleWebKit/537.36 (KHTML, like Gecko) " +
                                "Chrome/126.0.0.0 Safari/537.36")
                .setExtraHTTPHeaders(Map.of(
                        "Accept-Language", "ko-KR,ko;q=0.9,en-US;q=0.7,en;q=0.6"
                )));
    }
}
