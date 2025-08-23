// src/main/java/com/likelion/cleopatra/domain/population/dto/age/Ages.java
package com.likelion.cleopatra.domain.population.dto.age;

import com.likelion.cleopatra.domain.population.document.PopulationDoc;
import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Ages {
    private List<Long> resident;  // [10,20,30,40,50,60+ 순]
    private List<Double> percent; // [10,20,30,40,50,60+ 순]

    public static Ages from(PopulationDoc doc) {
        long total = doc.getTotalResident();
        long a10 = doc.getAge10Resident();
        long a20 = doc.getAge20Resident();
        long a30 = doc.getAge30Resident();
        long a40 = doc.getAge40Resident();
        long a50 = doc.getAge50Resident();
        long a60 = doc.getAge60PlusResident();

        double p10 = pct(a10, total);
        double p20 = pct(a20, total);
        double p30 = pct(a30, total);
        double p40 = pct(a40, total);
        double p50 = pct(a50, total);
        double p60 = pct(a60, total);

        return Ages.builder()
                .resident(List.of(a10, a20, a30, a40, a50, a60))
                .percent(List.of(p10, p20, p30, p40, p50, p60))
                .build();
    }

    private static double pct(long part, long total) {
        if (total <= 0) return 0.0;
        return Math.round(part * 1000.0 / total) / 10.0; // 소수1자리
    }
}
