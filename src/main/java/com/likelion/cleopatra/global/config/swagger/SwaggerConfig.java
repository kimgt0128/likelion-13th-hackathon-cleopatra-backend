// src/main/java/com/likelion/cleopatra/global/config/swagger/SwaggerConfig.java
package com.likelion.cleopatra.global.config.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI baseOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Cleopatra API")
                        .version("v1")
                        .description("멋쟁이 사자처럼 13기 연합 해커톤 구석구석 Swagger 문서"))
                .servers(List.of(new Server().url("/")));
    }
}
