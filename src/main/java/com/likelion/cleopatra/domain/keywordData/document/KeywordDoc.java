package com.likelion.cleopatra.domain.keywordData.document;

import com.likelion.cleopatra.global.common.enums.Platform;
import com.likelion.cleopatra.global.common.enums.address.District;
import com.likelion.cleopatra.global.common.enums.address.Neighborhood;
import com.likelion.cleopatra.global.common.enums.keyword.Primary;
import com.likelion.cleopatra.global.common.enums.keyword.Secondary;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "keyword_data")
public class KeywordDoc {

    @Id
    private String id;

    /** 수집 기준 키(예: 검색어) */
    private String keyword;

    private District district;
    private Neighborhood neighborhood;
    private Primary primary;
    private Secondary secondary;
    private Platform platform;
    private List<String> keywords;
    private String descript;

}
