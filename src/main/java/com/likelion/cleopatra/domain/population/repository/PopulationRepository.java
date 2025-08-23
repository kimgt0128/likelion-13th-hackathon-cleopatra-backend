package com.likelion.cleopatra.domain.population.repository;

import com.likelion.cleopatra.domain.population.document.PopulationDoc;

import java.util.Optional;

public interface PopulationRepository extends org.springframework.data.mongodb.repository.MongoRepository<PopulationDoc,String>{
    Optional<PopulationDoc> findByAdstrdName(String name);

}