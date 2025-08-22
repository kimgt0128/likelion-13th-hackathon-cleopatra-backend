package com.likelion.cleopatra.domain.income.repository;

import com.likelion.cleopatra.domain.income.document.IncomeDoc;

public interface IncomeRepository extends org.springframework.data.mongodb.repository.MongoRepository<IncomeDoc,String>{
    java.util.Optional<IncomeDoc> findByAdstrdCodeAndPeriod(String adstrdCode,int period);
}