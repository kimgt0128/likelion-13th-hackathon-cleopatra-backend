package com.likelion.cleopatra.domain.keywordData.repository;

import com.likelion.cleopatra.domain.keywordData.document.KeywordDoc;
import com.likelion.cleopatra.global.common.enums.address.District;
import com.likelion.cleopatra.global.common.enums.address.Neighborhood;
import com.likelion.cleopatra.global.common.enums.keyword.Primary;
import com.likelion.cleopatra.global.common.enums.keyword.Secondary;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface KeywordRepository extends MongoRepository<KeywordDoc, String> {
    List<KeywordDoc> findByDistrictAndNeighborhoodAndPrimaryAndSecondary(
            District district, Neighborhood neighborhood, Primary primary, Secondary secondary
    );
}