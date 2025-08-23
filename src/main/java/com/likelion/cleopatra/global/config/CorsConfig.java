package com.likelion.cleopatra.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override public void addCorsMappings(CorsRegistry r) {
        r.addMapping("/**")
                .allowedOrigins("https://www.guseokguseok.site","https://api.guseokguseok.site")
                .allowedMethods("GET","POST","PUT","PATCH","DELETE","OPTIONS")
                .allowedHeaders("*").exposedHeaders("*")
                .allowCredentials(true).maxAge(3600);
    }
}