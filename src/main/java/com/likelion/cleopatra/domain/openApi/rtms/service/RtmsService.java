package com.likelion.cleopatra.domain.openApi.rtms.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class RtmsService {

    @Qualifier("rtmsWebClient")
    private final WebClient rtmsWebClient;

    /**
     * 원본 XML 그대로 반환
     * @param lawdCd  법정동코드 앞 5자리 (예: 11110)
     * @param dealYmd 계약년월 6자리 (예: 202505)
     */
    public Mono<String> fetchRaw(String lawdCd, String dealYmd) {
        return rtmsWebClient.get()
                .uri(u -> u
                        .queryParam("LAWD_CD", lawdCd)
                        .queryParam("DEAL_YMD", dealYmd)
                        .build())
                .retrieve()
                .bodyToMono(String.class);
    }

    /**
     * 해당 월의 <dealAmount> 리스트 추출
     */
    public Mono<List<Long>> fetchDealAmounts(String lawdCd, String dealYmd) {
        return fetchRaw(lawdCd, dealYmd)
                .map(RtmsService::extractDealAmounts);
    }

    /**
     * 기준월 포함 최근 12개월 평균 금액. 데이터 없으면 0.0
     */
    public Mono<Double> avgLast12Months(String lawdCd, YearMonth anchorInclusive) {
        return Flux.range(0, 12)
                .map(anchorInclusive::minusMonths)
                .flatMap(ym -> {
                    String ymStr = ym.getYear() + String.format("%02d", ym.getMonthValue());
                    return fetchDealAmounts(lawdCd, ymStr).flatMapMany(Flux::fromIterable);
                })
                .collectList()
                .map(list -> list.isEmpty()
                        ? 0.0
                        : list.stream().mapToLong(v -> v).average().orElse(0.0));
    }

    /**
     * 동기 호출이 필요할 때 사용
     */
    public double avgLast12MonthsBlocking(String lawdCd, YearMonth anchorInclusive) {
        return avgLast12Months(lawdCd, anchorInclusive).blockOptional().orElse(0.0);
    }

    // MVP 파서: <dealAmount>300,000</dealAmount> → 300000
    private static List<Long> extractDealAmounts(String xml) {
        List<Long> out = new ArrayList<>();
        if (xml == null || xml.isEmpty()) return out;
        Matcher m = Pattern.compile("<dealAmount>([^<]+)</dealAmount>").matcher(xml);
        while (m.find()) {
            String raw = m.group(1).replace(",", "").trim();
            if (!raw.isEmpty()) {
                try { out.add(Long.parseLong(raw)); } catch (NumberFormatException ignored) {}
            }
        }
        return out;
    }
}