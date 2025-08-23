package com.likelion.cleopatra.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override public void addCorsMappings(CorsRegistry r) {
        r.addMapping("/**")
                .allowedOriginPatterns(
                        "https://www.guseokguseok.site",
                        "https://api.guseokguseok.site",
                        "http://localhost:*",
                        "http://127.0.0.1:*",
                        "http://192.168.*.*:*",   // LAN에서 직접 접속 시
                        "https://localhost:*",    // HTTPS 로컬 쓰면 추가
                        "https://127.0.0.1:*"
                )
                .allowedMethods("GET","POST","PUT","PATCH","DELETE","OPTIONS")
                .allowedHeaders("*").exposedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}