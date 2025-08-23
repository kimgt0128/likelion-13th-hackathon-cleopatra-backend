package com.likelion.cleopatra.domain.incomeConsumption.repository;

import com.likelion.cleopatra.domain.incomeConsumption.document.IncomeConsumptionDoc;

import java.util.Optional;

public interface IncomeConsumptionRepository extends org.springframework.data.mongodb.repository.MongoRepository<IncomeConsumptionDoc,String>{
    Optional<IncomeConsumptionDoc> findByAdstrdName(String name);
}