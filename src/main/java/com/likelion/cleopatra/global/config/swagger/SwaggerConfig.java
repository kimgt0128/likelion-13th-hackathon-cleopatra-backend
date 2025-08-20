// src/main/java/com/likelion/cleopatra/global/config/OpenApiConfig.java
package com.likelion.cleopatra.global.config.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
//@Profile("dev") // 개발 프로필에서만 노출
public class SwaggerConfig {

    @Bean
    public OpenAPI baseOpenAPI() {
        return new OpenAPI().info(
                new Info()
                        .title("Cleopatra API")
                        .version("v1")
                        .description("멋쟁이 사자처럼 13기 연합 해커톤 구석구석 Swagger 문서")
        );
    }


}