// src/main/java/com/likelion/cleopatra/global/config/OpenApiConfig.java
package com.likelion.cleopatra.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
//@Profile("dev") // 개발 프로필에서만 노출
public class OpenApiConfig {

    @Bean
    public OpenAPI baseOpenAPI() {
        return new OpenAPI().info(
                new Info()
                        .title("Cleopatra API")
                        .version("v1")
                        .description("수집/분석용 내부 API 문서")
        );
    }

    // 수집 API만 별도 그룹으로
    @Bean
    public GroupedOpenApi collectApi() {
        return GroupedOpenApi.builder()
                .group("collect")
                .packagesToScan("com.likelion.cleopatra.domain.data.controller")
                .pathsToMatch("/api/collect/**")
                .build();
    }
}