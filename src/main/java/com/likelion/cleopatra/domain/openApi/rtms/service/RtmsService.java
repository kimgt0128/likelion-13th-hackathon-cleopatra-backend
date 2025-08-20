// src/main/java/com/likelion/cleopatra/domain/openApi/rtms/service/RtmsService.java
package com.likelion.cleopatra.domain.openApi.rtms.service;

import com.likelion.cleopatra.domain.openApi.rtms.dto.RtmsRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
public class RtmsService {

    @Qualifier("rtmsWebClient")
    private final WebClient rtmsWebClient;

    private static final double PYEONG_TO_SQM = 3.305785;
    private static final double THRESHOLD_PYEONG = 30.0;
    private static final double THRESHOLD_SQM = THRESHOLD_PYEONG * PYEONG_TO_SQM; // 99.17355

    // 미리 컴파일한 패턴
    private static final Pattern ITEM_PATTERN   = Pattern.compile("<item>(.*?)</item>", Pattern.DOTALL);
    private static final Pattern AREA_PATTERN   = Pattern.compile("<buildingAr>([^<]*)</buildingAr>");
    private static final Pattern AMOUNT_PATTERN = Pattern.compile("<dealAmount>([^<]+)</dealAmount>");

    /** 원본 XML */
    public String fetchRaw(String lawdCd, String dealYmd) {
        log.debug("[RTMS] fetchRaw lawdCd={}, dealYmd={}", lawdCd, dealYmd);
        String xml = rtmsWebClient.get()
                .uri(u -> u.queryParam("LAWD_CD", lawdCd)
                        .queryParam("DEAL_YMD", dealYmd)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();

        int itemCnt = (xml == null) ? 0 : countItems(xml);
        log.debug("[RTMS] fetched ym={} items={} xmlSize={}B",
                dealYmd, itemCnt, xml == null ? 0 : xml.length());
        if (log.isTraceEnabled() && xml != null) {
            log.trace("[RTMS] xmlPreview(ym={}): {}",
                    dealYmd, xml.substring(0, Math.min(xml.length(), 500)).replaceAll("\\s+", " "));
        }
        return xml;
    }

    /** 12개월, 30평 미만/이상 평균·중위값(만원) + ㎡당/평당 평균가격(만원/㎡, 만원/평) */
    public RtmsRes avgBySizeLast12Months(String lawdCd, YearMonth anchorInclusive) {
        String anchorYm = anchorInclusive.getYear() + String.format("%02d", anchorInclusive.getMonthValue());
        log.debug("[RTMS] avgBySizeLast12Months start lawdCd={}, anchorYm={}, months=12", lawdCd, anchorYm);

        List<Double> smallAmt = new ArrayList<>();
        List<Double> largeAmt = new ArrayList<>();
        List<Double> smallPerSqm = new ArrayList<>();
        List<Double> largePerSqm = new ArrayList<>();

        for (int i = 0; i < 12; i++) {
            YearMonth ym = anchorInclusive.minusMonths(i);
            String ymStr = ym.getYear() + String.format("%02d", ym.getMonthValue());

            String xml = fetchRaw(lawdCd, ymStr);
            if (xml == null || xml.isEmpty()) {
                log.debug("[RTMS] ym={} no data", ymStr);
                continue;
            }

            int monthSmall = 0, monthLarge = 0, totalItems = 0, samplePrinted = 0;
            Matcher itemM = ITEM_PATTERN.matcher(xml);
            while (itemM.find()) {
                totalItems++;
                String item = itemM.group(1);

                Double areaSqm = extractDouble(item, AREA_PATTERN);
                if (areaSqm == null || areaSqm <= 0) continue;

                Double amountManWon = extractMoney(item, AMOUNT_PATTERN);
                if (amountManWon == null || amountManWon <= 0) continue;

                double perSqm = amountManWon / areaSqm; // 만원/㎡

                if (areaSqm < THRESHOLD_SQM) {
                    smallAmt.add(amountManWon);
                    smallPerSqm.add(perSqm);
                    monthSmall++;
                    if (log.isTraceEnabled() && samplePrinted < 5) {
                        log.trace("[RTMS] ym={} SMALL area={}㎡ amount={}만원 perSqm={}",
                                ymStr, round2(areaSqm), round1(amountManWon), round2(perSqm));
                        samplePrinted++;
                    }
                } else {
                    largeAmt.add(amountManWon);
                    largePerSqm.add(perSqm);
                    monthLarge++;
                    if (log.isTraceEnabled() && samplePrinted < 5) {
                        log.trace("[RTMS] ym={} LARGE area={}㎡ amount={}만원 perSqm={}",
                                ymStr, round2(areaSqm), round1(amountManWon), round2(perSqm));
                        samplePrinted++;
                    }
                }
            }
            log.debug("[RTMS] ym={} parsed items={}, small+= {}, large+= {}", ymStr, totalItems, monthSmall, monthLarge);
        }

        long smallCount = smallAmt.size();
        long largeCount = largeAmt.size();

        double smallAvgAmount  = avg(smallAmt);
        double largeAvgAmount  = avg(largeAmt);
        double smallMedAmount  = median(smallAmt);
        double largeMedAmount  = median(largeAmt);

        double smallAvgPerSqm  = avg(smallPerSqm);
        double largeAvgPerSqm  = avg(largePerSqm);
        double smallAvgPerPy   = smallAvgPerSqm * PYEONG_TO_SQM; // 만원/평
        double largeAvgPerPy   = largeAvgPerSqm * PYEONG_TO_SQM; // 만원/평

        log.debug("[RTMS] DONE lawdCd={}, months=12 smallCount={}, largeCount={}", lawdCd, smallCount, largeCount);
        log.debug("[RTMS] SMALL avg={}만원 median={}만원 perSqm={}만원/㎡ perPy={}만원/평",
                round1(smallAvgAmount), round1(smallMedAmount), round2(smallAvgPerSqm), round2(smallAvgPerPy));
        log.debug("[RTMS] LARGE avg={}만원 median={}만원 perSqm={}만원/㎡ perPy={}만원/평",
                round1(largeAvgAmount), round1(largeMedAmount), round2(largeAvgPerSqm), round2(largeAvgPerPy));

        return RtmsRes.builder()
                .lawdCd(lawdCd)
                .anchorYm(anchorYm)
                .months(12)
                .amountUnit("만원")
                .areaUnit("㎡")
                .unitPriceSqmUnit("만원/㎡")
                .unitPricePUnit("만원/평")
                .thresholdPyeong(THRESHOLD_PYEONG)
                .thresholdSqm(THRESHOLD_SQM)
                .smallCount(smallCount)
                .largeCount(largeCount)
                .smallAvgAmount(round1(smallAvgAmount))
                .largeAvgAmount(round1(largeAvgAmount))
                .smallMedianAmount(round1(smallMedAmount))
                .largeMedianAmount(round1(largeMedAmount))
                .smallAvgPerSqm(round2(smallAvgPerSqm))
                .largeAvgPerSqm(round2(largeAvgPerSqm))
                .smallAvgPerPyeong(round2(smallAvgPerPy))
                .largeAvgPerPyeong(round2(largeAvgPerPy))
                .build();
    }

    // ---------- helpers ----------
    private static int countItems(String xml) {
        int c = 0; Matcher m = ITEM_PATTERN.matcher(xml);
        while (m.find()) c++;
        return c;
    }

    private static double avg(List<Double> xs) {
        if (xs == null || xs.isEmpty()) return 0.0;
        double s = 0.0; for (double v : xs) s += v;
        return s / xs.size();
    }

    private static double median(List<Double> xs) {
        if (xs == null || xs.isEmpty()) return 0.0;
        List<Double> c = new ArrayList<>(xs);
        Collections.sort(c);
        int n = c.size();
        if ((n & 1) == 1) return c.get(n / 2);
        return (c.get(n / 2 - 1) + c.get(n / 2)) / 2.0;
    }

    /** money "300,000" → 300000.0 (만원) */
    private static Double extractMoney(String src, Pattern pattern) {
        Matcher m = pattern.matcher(src);
        if (!m.find()) return null;
        String raw = m.group(1);
        if (raw == null) return null;
        raw = raw.replace(",", "").trim();
        if (raw.isEmpty()) return null;
        try { return Double.parseDouble(raw); } catch (NumberFormatException e) { return null; }
    }

    private static Double extractDouble(String src, Pattern pattern) {
        Matcher m = pattern.matcher(src);
        if (!m.find()) return null;
        String raw = m.group(1);
        if (raw == null) return null;
        raw = raw.trim();
        if (raw.isEmpty()) return null;
        try { return Double.parseDouble(raw); } catch (NumberFormatException e) { return null; }
    }

    private static double round1(double v) { return Math.round(v * 10.0) / 10.0; }   // 금액
    private static double round2(double v) { return Math.round(v * 100.0) / 100.0; } // 단가
}
