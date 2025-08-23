package com.likelion.cleopatra.domain.keywordData.service;

import com.likelion.cleopatra.domain.collect.repository.ContentRepository;
import com.likelion.cleopatra.domain.keywordData.repository.KeywordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class KeywordService {

    // ContentRepository도 생성자로 주입받긴해야할듯?
    private ContentRepository contentRepository;
    private final WebClient webClient;

    public KeywordService(@Qualifier("keywordWebClient") WebClient webClient) {
        this.webClient = webClient;
    }

    /**
     * 쿼리에 대한 Content를 모두 찾아서 플랫폼별(NAVER_BLOG, NAVER_PLACE, YOUTUE)로 다음형태로 만들어 요청
     * {
     * 	"areaa": "노원구 공릉동",
     * 	"keyword": "외식업 일식",
     * 	"data": [
     * 		"data_naver_blog": [
     * 		            {
     * 		    "doc_id": "nb1",
     * 		    "platform": "NAVER_BLOG",
     * 		    "text": "면이 되게 쫄깃하고 맛있어요!!! 면 양을 조절할수도있고 양도 많아요😊 ..."
     *          },
     *          {
     * 		    "doc_id": "nb2",
     * 		    "platform": "baemin",
     * 		    "text": "저녁에 방문... 시그니처 메뉴 💓 특히 치즈얹은 밥이랑 카레 조합은 정말 최고였어요..."
     *          },
     *          {
     * 		    "doc_id": "nb3",
     * 		    "platform": "baemin",
     * 		    "text": "점심에 방문... 주문하자마자 나오는 속도도 청결도 모든게 다 만족했습니다 >< 또 올거예요."
     *          },
     * 		  ... 네이버 블로그 30개
     * 		],
     * 	 "data_naver_palce": [      * 	  {
     * 	    "doc_id": "np1",
     * 	    "platform": "NAVER_PLACE",
     * 	    "text": "가성비 좋은 메뉴가 많고 양도 푸짐합니다. 특히 김치찌개가 진하고 맛있었어요."
     *      },
     *      {
     * 	    "doc_id": "np2",
     * 	    "platform": "NAVER_PLACE",
     * 	    "text": "매장이 깔끔하고 분위기도 좋아서 친구랑 오기 딱 좋네요. 다만 저녁에는 조금 시끄러워요."
     *      },
     * 	  ... 네이버 플레이스 30개
     *   ],
     *   "data_youtube": [
     *      {
     * 	    "doc_id": "yt1",
     * 	    "platform": "YOUTUBE",
     * 	    "text": "오늘은 공릉역에 있는 초밥 맛집에 다녀왔습니다 ~~ 이렇게 역 바로앞에 있어서~~"
     *      },
     *      {
     * 	    "doc_id": "yt1",
     * 	    "platform": "YOUTUBE",
     * 	    "text": "오늘은 공릉역에 있는 초밥 맛집에 다녀왔습니다 ~~ 이렇게 역 바로앞에 있어서~~"
     *      },
     *      {
     * 	    "doc_id": "yt2",
     * 	    "platform": "YOUTUBE",
     * 	    "text": "오늘은 공릉역에 있는 초밥 맛집에 다녀왔습니다 ~~ 이렇게 역 바로앞에 있어서~~"
     *      },
     * 	  ... 유튜브 30개
     * 	]
     * }
     * @param query
     * @return
     */
    public KeywordDescriptionRes getDescription(String query) {
        // 이 키워드는 검색 쿼리임("공릉 일식")
        contentRepository.findByKeyword(query);
    }
}
