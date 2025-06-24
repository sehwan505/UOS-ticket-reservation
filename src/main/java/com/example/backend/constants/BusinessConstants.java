package com.example.backend.constants;

/**
 * 비즈니스 로직에서 사용되는 숫자 상수 관리 클래스
 */
public class BusinessConstants {
    
    /**
     * 포인트 관련 상수
     */
    public static class Points {
        public static final double EARNING_RATE = 0.05;    // 예매 금액의 5% 적립
        public static final int MIN_POINTS = 10;           // 최소 적립 포인트
        public static final int MAX_POINTS = 1000;         // 최대 적립 포인트
        public static final int INITIAL_POINTS = 0;        // 초기 포인트
    }
    
    /**
     * 결제 관련 상수
     */
    public static class Payment {
        public static final int TIMEOUT_MINUTES = 30;      // 결제 타임아웃 (분)
        public static final int SUCCESS_RATE = 95;         // 은행 API 성공률 (%)
        public static final int CANCEL_SUCCESS_RATE = 98;  // 취소 성공률 (%)
        public static final long PROCESSING_DELAY_MS = 1000; // 처리 지연 시간 (ms)
    }
    
    /**
     * 평점 관련 상수
     */
    public static class Rating {
        public static final double INITIAL_RATING = 0.0;   // 신규 영화 초기 평점
        public static final int MIN_RATING = 1;            // 최소 평점
        public static final int MAX_RATING = 5;            // 최대 평점
    }
    
    /**
     * 좌석 관련 상수
     */
    public static class Seat {
        public static final int DEFAULT_SEATS_PER_ROW = 15;    // 기본 한 줄당 좌석 수
        public static final int MIN_TOTAL_SEATS = 80;          // 최소 총 좌석 수
        public static final int MAX_TOTAL_SEATS = 140;         // 최대 총 좌석 수
        public static final int COUPLE_SEAT_MAX_PER_ROW = 10;  // 커플석 한 줄 최대 수
        
        // 좌석 등급별 배치 비율
        public static final double STANDARD_RATIO = 0.4;       // 앞쪽 40%: 일반석
        public static final double PREMIUM_RATIO = 0.7;        // 중간 30%: 프리미엄
        public static final double VIP_RATIO = 0.9;            // 뒤쪽 20%: VIP석
        // 맨 뒤 10%: 커플석
    }
    
    /**
     * 스케줄링 관련 상수
     */
    public static class Schedule {
        public static final int CHECK_INTERVAL_MINUTES = 10;   // 미결제 예약 체크 간격 (분)
        public static final int LOG_INTERVAL_HOURS = 1;        // 로깅 간격 (시간)
        public static final int WARNING_THRESHOLD = 10;        // 미결제 예약 경고 임계값
    }
    
    /**
     * 상영관 관련 상수
     */
    public static class Screen {
        public static final int MIN_SCREENS_PER_CINEMA = 3;    // 영화관당 최소 상영관 수
        public static final int MAX_SCREENS_PER_CINEMA = 5;    // 영화관당 최대 상영관 수
    }
    
    /**
     * 트랜잭션 타임아웃 상수
     */
    public static class Transaction {
        public static final int RESERVATION_TIMEOUT_SECONDS = 30;  // 예매 트랜잭션 타임아웃
        public static final int READ_TIMEOUT_SECONDS = 10;         // 읽기 전용 트랜잭션 타임아웃
    }
    
    /**
     * 랜덤 생성 관련 상수
     */
    public static class Random {
        public static final int ERROR_CODE_RANGE = 1000;       // 에러 코드 랜덤 범위
        public static final int APPROVAL_NUMBER_RANGE = 1000;  // 승인번호 랜덤 범위
        public static final double TICKET_ISSUANCE_RATE = 0.3; // 발권 완료 비율 (30%)
    }
    
    /**
     * 문자열 포맷 관련 상수
     */
    public static class Format {
        public static final String TWO_DIGIT_FORMAT = "%02d";  // 2자리 숫자 포맷
        public static final String FOUR_DIGIT_FORMAT = "%04d"; // 4자리 숫자 포맷
        public static final int COLUMN_FORMAT_WIDTH = 2;       // 열 번호 포맷 너비
    }
} 