package sehwan505.uosticketreservation.constants;

/**
 * 지역 관련 상수 관리 클래스
 */
public class RegionConstants {
    
    /**
     * 지역 정보
     */
    public enum Region {
        SEOUL("01", "서울"),
        GYEONGGI("02", "경기"),
        BUSAN("03", "부산"),
        DAEGU("04", "대구"),
        INCHEON("05", "인천"),
        GWANGJU("06", "광주"),
        DAEJEON("07", "대전"),
        ULSAN("08", "울산"),
        GANGWON("09", "강원"),
        CHUNGBUK("10", "충북");
        
        private final String code;
        private final String name;
        
        Region(String code, String name) {
            this.code = code;
            this.name = name;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getName() {
            return name;
        }
        
        /**
         * 코드로 지역 찾기
         */
        public static Region findByCode(String code) {
            for (Region region : values()) {
                if (region.getCode().equals(code)) {
                    return region;
                }
            }
            throw new IllegalArgumentException("Invalid region code: " + code);
        }
    }
} 