// src/main/java/com/likelion/cleopatra/domain/population/dto/dto/Description.java
package com.likelion.cleopatra.domain.population.dto.description;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class DescriptionPopulation {
    private String ageDescription;
    private String genderDescription;

    public static DescriptionPopulation of(String ageDesc, String genderDesc) {
        return DescriptionPopulation.builder()
                .ageDescription(ageDesc)
                .genderDescription(genderDesc)
                .build();
    }
}
