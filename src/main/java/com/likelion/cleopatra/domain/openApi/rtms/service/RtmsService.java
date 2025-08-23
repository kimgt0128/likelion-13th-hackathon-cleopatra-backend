// src/main/java/com/likelion/cleopatra/domain/openApi/rtms/service/RtmsService.java
package com.likelion.cleopatra.domain.openApi.rtms.service;

import com.likelion.cleopatra.domain.report.dto.price.PriceRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.YearMonth;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * RTMS 계산 전담.
 * - 소형=50평 이상, 대형=50평 미만
 * - dongFilter(umdNm 일치)만 집계
 * - 월별 요청에 numOfRows=1000 적용
 * - 결과 단위: 금액=만원, ㎡당=만원/㎡, 평당=만원/평
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RtmsService {

    @Qualifier("rtmsWebClient")
    private final WebClient rtmsWebClient;

    private static final double PYEONG_TO_SQM = 3.305785;
    private static final double THRESHOLD_PYEONG = 50.0;
    private static final double THRESHOLD_SQM = THRESHOLD_PYEONG * PYEONG_TO_SQM; // 165.28925
    private static final int NUM_OF_ROWS = 1000;

    private static final Pattern ITEM   = Pattern.compile("<item>(.*?)</item>", Pattern.DOTALL);
    private static final Pattern AREA   = Pattern.compile("<buildingAr>([^<]*)</buildingAr>");
    private static final Pattern AMOUNT = Pattern.compile("<dealAmount>([^<]+)</dealAmount>");
    private static final Pattern UMD    = Pattern.compile("<umdNm>([^<]+)</umdNm>");

    /** 월별 XML 원문 획득 */
    private String fetchRaw(String lawdCd, String dealYmd) {
        String xml = rtmsWebClient.get()
                .uri(u -> u.queryParam("LAWD_CD", lawdCd)
                        .queryParam("DEAL_YMD", dealYmd)
                        .queryParam("numOfRows", NUM_OF_ROWS)
                        .build())
                .retrieve()
                .bodyToMono(String.class)
                .block();
        log.debug("[RTMS] ym={} fetched {}B", dealYmd, xml==null?0:xml.length());
        return xml;
    }

    /** 12개월 집계 후 PriceRes 생성. */
    public PriceRes buildPriceRes(String lawdCd, YearMonth anchorInclusive, String dongFilter) {
        String anchorYm = ym(anchorInclusive);
        String dongKey = normKo(dongFilter);
        log.debug("[RTMS] buildPriceRes lawdCd={}, anchorYm={}, dong={}", lawdCd, anchorYm, dongFilter);

        List<Double> smallAmt = new ArrayList<>(); // 50평 이상
        List<Double> bigAmt   = new ArrayList<>(); // 50평 미만
        List<Double> perSqmAll= new ArrayList<>();

        Map<String, Long> qCnt = new TreeMap<>();

        for (int i = 0; i < 12; i++) {
            YearMonth ym = anchorInclusive.minusMonths(i);
            String ymStr = ym(ym);

            String xml = fetchRaw(lawdCd, ymStr);
            if (xml == null || xml.isEmpty()) { log.debug("[RTMS] ym={} empty", ymStr); continue; }

            long total=0, matched=0;
            Matcher it = ITEM.matcher(xml);
            while (it.find()) {
                total++;
                String item = it.group(1);

                String umdNm = extractText(item, UMD);
                if (umdNm == null || !normKo(umdNm).equals(dongKey)) continue;

                Double areaSqm = extractDouble(item, AREA);
                Double amount  = extractMoney(item, AMOUNT);
                if (areaSqm == null || areaSqm <= 0 || amount == null || amount <= 0) continue;

                matched++;
                double perSqm = amount / areaSqm;
                perSqmAll.add(perSqm);

                if (areaSqm >= THRESHOLD_SQM) smallAmt.add(amount);
                else                          bigAmt.add(amount);
            }
            String qKey = quarterKey(ym);
            qCnt.put(qKey, qCnt.getOrDefault(qKey, 0L) + matched);
            log.debug("[RTMS] ym={} items={} matched(dong)={} -> {}", ymStr, total, matched, qKey);
        }

        long smallCount = smallAmt.size();
        long bigCount   = bigAmt.size();

        double smallAvg = avg(smallAmt), bigAvg = avg(bigAmt);
        double smallMed = median(smallAmt), bigMed = median(bigAmt);
        double perSqmAvg = avg(perSqmAll), perPyAvg = perSqmAvg * PYEONG_TO_SQM;

        log.debug("[RTMS] SMALL cnt={} avg={} med={}", smallCount, round1(smallAvg), round1(smallMed));
        log.debug("[RTMS] BIG   cnt={} avg={} med={}", bigCount,   round1(bigAvg),   round1(bigMed));
        log.debug("[RTMS] Unit  perSqm={} perPy={}", round2(perSqmAvg), round2(perPyAvg));

        return PriceRes.builder()
                .lawdCd(lawdCd)
                .anchorYm(anchorYm)
                .months(12)
                .smallCount(smallCount)
                .smallAverage(round1(smallAvg))
                .smallMedian(round1(smallMed))
                .bigCount(bigCount)
                .bigAverage(round1(bigAvg))
                .bigMedian(round1(bigMed))
                .pricePerMeter(round2(perSqmAvg))
                .pricePerPyeong(round2(perPyAvg))
                .tradingVolume(qCnt)
                .build();
    }

    // ---- helpers ----
    private static String ym(YearMonth ym){ return ym.getYear()+String.format("%02d", ym.getMonthValue()); }
    private static String quarterKey(YearMonth ym){ int q=(ym.getMonthValue()-1)/3+1; return ym.getYear()+"_"+q+"_quarter"; }

    /** 비교용 정규화: 공백 제거 + 끝의 숫자+동 → 동 */
    private static String normKo(String s){
        if (s == null) return "";
        String z = s.replaceAll("\\s+","");
        z = z.replaceAll("\\d+동$", "동");
        return z;
    }

    private static double avg(List<Double> xs){ if(xs==null||xs.isEmpty())return 0.0; double s=0; for(double v:xs)s+=v; return s/xs.size(); }
    private static double median(List<Double> xs){
        if(xs==null||xs.isEmpty())return 0.0; List<Double> c=new ArrayList<>(xs); Collections.sort(c);
        int n=c.size(); return (n&1)==1? c.get(n/2): (c.get(n/2-1)+c.get(n/2))/2.0;
    }
    private static String extractText(String src, Pattern p){ Matcher m=p.matcher(src); return m.find()? m.group(1).trim(): null; }
    /** "12,345" → 12345.0(만원) */
    private static Double extractMoney(String src, Pattern p){
        Matcher m=p.matcher(src); if(!m.find())return null; String raw=m.group(1); if(raw==null)return null;
        raw=raw.replace(",","").trim(); if(raw.isEmpty())return null; try{ return Double.parseDouble(raw);}catch(Exception e){return null;}
    }
    private static Double extractDouble(String src, Pattern p){
        Matcher m=p.matcher(src); if(!m.find())return null; String raw=m.group(1); if(raw==null)return null;
        raw=raw.trim(); if(raw.isEmpty())return null; try{ return Double.parseDouble(raw);}catch(Exception e){return null;}
    }
    private static double round1(double v){ return Math.round(v*10.0)/10.0; }
    private static double round2(double v){ return Math.round(v*100.0)/100.0; }
}
