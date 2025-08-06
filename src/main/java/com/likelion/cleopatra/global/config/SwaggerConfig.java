package com.likelion.cleopatra.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Cleopatra API Docs")
                        .description("멋쟁이 사자처럼 13기 연합 해커톤 구석구석 Swagger 문서")
                        .version("1.0.0"));
    }
}
