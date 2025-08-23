// src/main/java/com/likelion/cleopatra/domain/population/document/PopulationDoc.java
package com.likelion.cleopatra.domain.population.document;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Document("population")
@CompoundIndex(name = "uk_pop_adstrd_period", def = "{'adstrdCode':1,'period':1}", unique = true)
public class PopulationDoc {
    @Id private String id;

    @Indexed private int period;        // 20251
    @Indexed private String adstrdCode; // 행정동 코드
    private String adstrdName;

    private long totalResident;
    private long maleResident;
    private long femaleResident;

    private long age10Resident;
    private long age20Resident;
    private long age30Resident;
    private long age40Resident;
    private long age50Resident;
    private long age60PlusResident;

    private long maleAge10Resident;
    private long maleAge20Resident;
    private long maleAge30Resident;
    private long maleAge40Resident;
    private long maleAge50Resident;
    private long maleAge60PlusResident;

    private long femaleAge10Resident;
    private long femaleAge20Resident;
    private long femaleAge30Resident;
    private long femaleAge40Resident;
    private long femaleAge50Resident;
    private long femaleAge60PlusResident;

    private long totalHouseholds;
    private long apartmentHouseholds;
    private long nonApartmentHouseholds;
}
