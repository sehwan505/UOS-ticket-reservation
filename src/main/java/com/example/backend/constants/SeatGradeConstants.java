package com.example.backend.constants;

/**
 * 좌석 등급 관련 상수 관리 클래스
 */
public class SeatGradeConstants {
    
    /**
     * 좌석 등급 정보
     */
    public enum SeatGrade {
        STANDARD(StatusConstants.SeatGrade.STANDARD, "일반석", 12000),
        PREMIUM(StatusConstants.SeatGrade.PREMIUM, "프리미엄", 15000),
        COUPLE(StatusConstants.SeatGrade.COUPLE, "커플석", 18000),
        VIP(StatusConstants.SeatGrade.VIP, "VIP석", 22000);
        
        private final String code;
        private final String name;
        private final int price;
        
        SeatGrade(String code, String name, int price) {
            this.code = code;
            this.name = name;
            this.price = price;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getName() {
            return name;
        }
        
        public int getPrice() {
            return price;
        }
        
        /**
         * 코드로 좌석 등급 찾기
         */
        public static SeatGrade findByCode(String code) {
            for (SeatGrade grade : values()) {
                if (grade.getCode().equals(code)) {
                    return grade;
                }
            }
            throw new IllegalArgumentException("Invalid seat grade code: " + code);
        }
    }
} 