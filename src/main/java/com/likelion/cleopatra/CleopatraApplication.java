package com.likelion.cleopatra;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan // application-webclient.yml의 prefix 매핑 활성화
@SpringBootApplication
public class CleopatraApplication {

    public static void main(String[] args) {
        SpringApplication.run(CleopatraApplication.class, args);
    }

}
