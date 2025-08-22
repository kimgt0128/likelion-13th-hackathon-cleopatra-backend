// src/main/java/com/likelion/cleopatra/domain/population/dto/description/Description.java
package com.likelion.cleopatra.domain.population.dto.description;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Description {
    private String ageDescription;
    private String genderDescription;

    public static Description of(String ageDesc, String genderDesc) {
        return Description.builder()
                .ageDescription(ageDesc)
                .genderDescription(genderDesc)
                .build();
    }
}
