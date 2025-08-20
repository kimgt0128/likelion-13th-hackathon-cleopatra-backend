// src/main/java/com/likelion/cleopatra/domain/openApi/rtms/service/RtmsService.java
package com.likelion.cleopatra.domain.openApi.rtms.service;

import com.likelion.cleopatra.domain.openApi.rtms.dto.RtmsRes;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.YearMonth;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class RtmsService {

    @Qualifier("rtmsWebClient")
    private final WebClient rtmsWebClient;

    private static final double PYEONG_TO_SQM = 3.305785;
    private static final double THRESHOLD_PYEONG = 30.0;
    private static final double THRESHOLD_SQM = THRESHOLD_PYEONG * PYEONG_TO_SQM; // 99.17355

    /** 원본 XML */
    public String fetchRaw(String lawdCd, String dealYmd) {
        return rtmsWebClient.get()
                .uri(u -> u.queryParam("LAWD_CD", lawdCd)
                        .queryParam("DEAL_YMD", dealYmd)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    /**
     * 기준월 포함 최근 12개월, 30평 미만/이상 평균(만원) 계산.
     * 반환은 동기 DTO.
     */
    public RtmsRes avgBySizeLast12Months(String lawdCd, YearMonth anchorInclusive) {
        double smallSum = 0.0; long smallCnt = 0;
        double largeSum = 0.0; long largeCnt = 0;

        for (int i = 0; i < 12; i++) {
            YearMonth ym = anchorInclusive.minusMonths(i);
            String ymStr = ym.getYear() + String.format("%02d", ym.getMonthValue());
            String xml = fetchRaw(lawdCd, ymStr);
            if (xml == null || xml.isEmpty()) continue;

            // item 단위로 파싱 → 각 item에서 buildingAr와 dealAmount 함께 추출
            Matcher itemM = Pattern.compile("<item>(.*?)</item>", Pattern.DOTALL).matcher(xml);
            while (itemM.find()) {
                String item = itemM.group(1);

                Double areaSqm = extractDouble(item, "<buildingAr>([^<]*)</buildingAr>");
                // area가 없거나 0이면 제외
                if (areaSqm == null || areaSqm <= 0) continue;

                Double amountManWon = extractMoney(item, "<dealAmount>([^<]+)</dealAmount>");
                if (amountManWon == null || amountManWon <= 0) continue;

                if (areaSqm < THRESHOLD_SQM) {
                    smallSum += amountManWon;
                    smallCnt++;
                } else {
                    largeSum += amountManWon;
                    largeCnt++;
                }
            }
        }

        double smallAvg = smallCnt == 0 ? 0.0 : smallSum / smallCnt;
        double largeAvg = largeCnt == 0 ? 0.0 : largeSum / largeCnt;

        YearMonth a = anchorInclusive;
        String anchorYm = a.getYear() + String.format("%02d", a.getMonthValue());

        return RtmsRes.builder()
                .lawdCd(lawdCd)
                .anchorYm(anchorYm)
                .months(12)
                .amountUnit("만원")
                .areaUnit("㎡")
                .thresholdPyeong(THRESHOLD_PYEONG)
                .thresholdSqm(THRESHOLD_SQM)
                .smallCount(smallCnt)
                .largeCount(largeCnt)
                .smallAvgAmount(round1(smallAvg))
                .largeAvgAmount(round1(largeAvg))
                .build();
    }

    // --------- helpers ---------

    /** money 문자열 "300,000" → 300000.0 (만원 단위 유지) */
    private static Double extractMoney(String src, String regex) {
        Matcher m = Pattern.compile(regex).matcher(src);
        if (!m.find()) return null;
        String raw = m.group(1);
        if (raw == null) return null;
        raw = raw.replace(",", "").trim();
        if (raw.isEmpty()) return null;
        try { return Double.parseDouble(raw); } catch (NumberFormatException e) { return null; }
    }

    /** double 필드 추출 */
    private static Double extractDouble(String src, String regex) {
        Matcher m = Pattern.compile(regex).matcher(src);
        if (!m.find()) return null;
        String raw = m.group(1);
        if (raw == null) return null;
        raw = raw.trim();
        if (raw.isEmpty()) return null;
        try { return Double.parseDouble(raw); } catch (NumberFormatException e) { return null; }
    }

    private static double round1(double v) {
        return Math.round(v * 10.0) / 10.0;
    }
}
