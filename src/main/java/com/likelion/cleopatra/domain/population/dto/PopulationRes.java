// src/main/java/com/likelion/cleopatra/domain/report/dto/population/PopulationRes.java
package com.likelion.cleopatra.domain.population.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.likelion.cleopatra.domain.population.dto.age.Ages;
import com.likelion.cleopatra.domain.population.dto.description.DescriptionPopulation;
import com.likelion.cleopatra.domain.population.dto.gender.Gender;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class PopulationRes {
    private long totalResident;
    private Ages ages;
    private Gender gender;

    public static PopulationRes from(Ages ages, Gender gender) {
        return PopulationRes.builder()
                .ages(ages)
                .gender(gender).build();
    }
}