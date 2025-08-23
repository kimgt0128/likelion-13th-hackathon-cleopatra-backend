// src/main/java/com/likelion/cleopatra/domain/population/dto/gender/Gender.java
package com.likelion.cleopatra.domain.population.dto.gender;

import com.likelion.cleopatra.domain.population.document.PopulationDoc;
import lombok.*;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Gender {
    private List<Long> resident;   // [male, female]
    private List<Double> percent;  // [male, female]

    // Resident
    private long maleResident;
    private long femaleResident;

    // Percent
    private double malePercent;
    private double femalePercent;

    public static Gender from(PopulationDoc doc) {
        long total = doc.getTotalResident();
        long male = doc.getMaleResident();
        long female = doc.getFemaleResident();

        double pm = pct(male, total);
        double pf = pct(female, total);

        return Gender.builder()
                .resident(List.of(male, female))
                .percent(List.of(pm, pf))
                .maleResident(male).femaleResident(female)
                .malePercent(pm).femalePercent(pf)
                .build();
    }

    private static double pct(long part, long total) {
        if (total <= 0) return 0.0;
        return Math.round(part * 1000.0 / total) / 10.0;
    }
}
