package com.example.backend.constants;

/**
 * 시스템에서 사용되는 상태 코드 상수 관리 클래스
 */
public class StatusConstants {
    
    /**
     * 예매 상태 코드
     */
    public static class Reservation {
        public static final String NOT_COMPLETED = "N";   // 예매 미완료
        public static final String CANCELLED = "D";       // 예매 취소 중
        public static final String COMPLETED = "Y";       // 예매 완료
    }
    
    /**
     * 결제 상태 코드
     */
    public static class Payment {
        public static final String NOT_COMPLETED = "N";   // 결제 미완료
        public static final String PROCESSING = "D";      // 결제 중
        public static final String COMPLETED = "Y";       // 결제 완료
    }
    
    /**
     * 발권 상태 코드
     */
    public static class TicketIssuance {
        public static final String NOT_ISSUED = "N";      // 미발권
        public static final String ISSUED = "Y";          // 발권 완료
    }
    
    /**
     * 포인트 히스토리 상태 코드
     */
    public static class PointHistory {
        public static final String ACCUMULATE = "A";      // 적립
        public static final String USE = "U";             // 사용
        public static final String EXPIRE = "E";          // 소멸
    }
    
    /**
     * 영화 상영 상태 코드
     */
    public static class Movie {
        public static final String SCREENING = "Y";       // 상영중
        public static final String NOT_SCREENING = "N";   // 상영 예정
        public static final String ENDED = "D";           // 상영 종료
    }
    
    /**
     * 좌석 등급 코드
     */
    public static class SeatGrade {
        public static final String STANDARD = "A";        // 일반석
        public static final String PREMIUM = "B";         // 프리미엄석
        public static final String COUPLE = "C";          // 커플석
        public static final String VIP = "D";             // VIP석
    }
    
    /**
     * 회원 등급 코드
     */
    public static class MemberGrade {
        public static final String BASIC = "B";          // 기본 등급
        public static final String SILVER = "S";         // 실버 등급
        public static final String GOLD = "G";           // 골드 등급
        public static final String PLATINUM = "P";       // 플래티넘 등급
        public static final String DIAMOND = "D";       // 다이아몬드 등급
        public static final String ADMIN = "A";         // 관리자 등급
    }
    
    /**
     * 예약 전달 상태 코드
     */
    public static class Transfer {
        public static final String TRANSFERRED = "Y";     // 전달됨
        public static final String NOT_TRANSFERRED = "N"; // 전달안됨
    }
    
    /**
     * 할인 코드
     */
    public static class DiscountCode {
        public static final String YOUTH_SENIOR = "A";    // 청소년/노인 할인
        public static final String NIGHT_EARLY = "B";     // 심야/조조 할인
        public static final String BOTH = "C";        // 둘 다
        
        // 할인 금액
        public static final int YOUTH_SENIOR_AMOUNT = 2000;  // 청소년/노인 할인 2000원
        public static final int NIGHT_EARLY_AMOUNT = 3000;   // 심야/조조 할인 3000원
        public static final int BOTH_AMOUNT = 5000;      // 둘 다 할인 3000원
    }

    /**
     * 상태 설명을 반환하는 유틸리티 메서드들
     */
    public static class Description {
        
        public static String getReservationStatus(String status) {
            return switch (status) {
                case Reservation.NOT_COMPLETED -> "예매 미완료";
                case Reservation.CANCELLED -> "예매 취소 중";
                case Reservation.COMPLETED -> "예매 완료";
                default -> "알 수 없는 상태";
            };
        }
        
        public static String getPaymentStatus(String status) {
            return switch (status) {
                case Payment.NOT_COMPLETED -> "결제 미완료";
                case Payment.PROCESSING -> "결제 중";
                case Payment.COMPLETED -> "결제 완료";
                default -> "알 수 없는 상태";
            };
        }
        
        public static String getPointHistoryStatus(String status) {
            return switch (status) {
                case PointHistory.ACCUMULATE -> "적립";
                case PointHistory.USE -> "사용";
                case PointHistory.EXPIRE -> "소멸";
                default -> "알 수 없는 상태";
            };
        }
        
        public static String getMovieStatus(String status) {
            return switch (status) {
                case Movie.SCREENING -> "상영중";
                case Movie.ENDED -> "상영 종료";
                default -> "알 수 없는 상태";
            };
        }
        
        public static String getTicketIssuanceStatus(String status) {
            return TicketIssuance.ISSUED.equals(status) ? "발권 완료" : "미발권";
        }
        
        public static String getDiscountCodeDescription(String discountCode) {
            return switch (discountCode) {
                case DiscountCode.YOUTH_SENIOR -> "청소년/노인 할인";
                case DiscountCode.NIGHT_EARLY -> "심야/조조 할인";
                case DiscountCode.BOTH -> "둘 다 할인";
                default -> "할인 없음";
            };
        }
        
        public static int getDiscountAmount(String discountCode) {
            return switch (discountCode) {
                case DiscountCode.YOUTH_SENIOR -> DiscountCode.YOUTH_SENIOR_AMOUNT;
                case DiscountCode.NIGHT_EARLY -> DiscountCode.NIGHT_EARLY_AMOUNT;
                case DiscountCode.BOTH -> DiscountCode.BOTH_AMOUNT;
                default -> 0;
            };
        }
        
        public static String getTransferStatus(String status) {
            return switch (status) {
                case Transfer.TRANSFERRED -> "전달됨";
                case Transfer.NOT_TRANSFERRED -> "전달안됨";
                default -> "알 수 없는 상태";
            };
        }
    }
} 