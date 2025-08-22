package com.likelion.cleopatra.domain.population.repository;

import com.likelion.cleopatra.domain.population.document.PopulationDoc;

public interface PopulationRepository extends org.springframework.data.mongodb.repository.MongoRepository<PopulationDoc,String>{
    java.util.Optional<PopulationDoc> findByAdstrdCodeAndPeriod(String code, int period);
}