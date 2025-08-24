package com.likelion.cleopatra.global.geo;

import com.likelion.cleopatra.global.common.enums.address.District;
import com.likelion.cleopatra.global.common.enums.address.Neighborhood;

import java.util.HashMap;
import java.util.Map;

/**
 * 서울 자치구 → LAWD_CD(앞 5자리) 매핑, 동명 선택/정규화 유틸.
 * - resolveGuCode5: 구명으로 5자리 코드 반환
 * - pickDongOrThrow: sub_neighborhood 우선, 없으면 neighborhood. "공릉 1동" → "공릉동" 정규화
 */
public final class LawdCodeResolver {
    private LawdCodeResolver() {}

    private static final Map<String, String> GU = new HashMap<>();
    static {
        put("종로구","11110"); put("중구","11140"); put("용산구","11170");
        put("성동구","11200"); put("광진구","11215"); put("동대문구","11230");
        put("중랑구","11260"); put("성북구","11290"); put("강북구","11305");
        put("도봉구","11320"); put("노원구","11350"); put("은평구","11380");
        put("서대문구","11410"); put("마포구","11440"); put("양천구","11470");
        put("강서구","11500"); put("구로구","11530"); put("금천구","11545");
        put("영등포구","11560"); put("동작구","11590"); put("관악구","11620");
        put("서초구","11650"); put("강남구","11680"); put("송파구","11710");
        put("강동구","11740");
    }

    /** enum 기반 조회 */
    public static String resolveGuCode5(District district){
        String code = GU.get(district.getKo());
        if (code == null) throw new IllegalArgumentException("자치구 미지원: " + district);
        return code;
    }

    /** enum + 서브동 문자열 → 법정동 정규화 */
    public static String pickDongOrThrow(Neighborhood neighborhood, String subNeighborhood){
        String base = (subNeighborhood != null && !subNeighborhood.isBlank())
                ? subNeighborhood
                : neighborhood.toJson();   // enum이 getKo()면 neighborhood.getKo()로 교체
        return toBeopjeongDong(base);
    }

    /** "공릉 1동" → "공릉동" 정규화. */
    private static String toBeopjeongDong(String name){
        String s = name.replaceAll("\\s+","");  // 공백 제거
        s = s.replaceAll("\\d+동$", "동");      // 숫자+동 → 동
        return s;
    }

    private static void put(String n, String c){ GU.put(norm(n), c); }
    private static String norm(String s){ return s==null? "": s.replaceAll("\\s+","").toLowerCase(); }
    private static boolean isBlank(String s){ return s==null || s.trim().isEmpty(); }
}
