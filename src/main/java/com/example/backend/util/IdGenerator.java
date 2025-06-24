package com.example.backend.util;

import com.example.backend.constants.BusinessConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Random;

/**
 * 각종 ID 생성을 담당하는 유틸리티 클래스
 */
@Slf4j
@Component
public class IdGenerator {
    
    private final Random random = new Random();
    
    /**
     * 예매 ID 생성
     * 형식: {스케줄ID}{좌석ID}{일일예매순서(2자리)}
     */
    public String generateReservationId(String scheduleId, String seatId, int dailyCount) {
        return scheduleId + seatId + String.format(BusinessConstants.Format.TWO_DIGIT_FORMAT, dailyCount);
    }
    
    /**
     * 스케줄 ID 생성
     * 형식: YYMMDD + 상영관번호 + 일일상영순서(2자리)
     */
    public String generateScheduleId(String date, String screenId, int dailyOrder) {
        // date는 YYYYMMDD 형식이므로 YYMM으로 변환
        String yearMonth = date.substring(2, 6); // YYMM
        String day = date.substring(6, 8); // DD
        
        return yearMonth + day + screenId + String.format(BusinessConstants.Format.TWO_DIGIT_FORMAT, dailyOrder);
    }
    
    /**
     * 승인번호 생성
     * 형식: {prefix}{timestamp}{random}
     */
    public String generateApprovalNumber(String prefix) {
        return prefix + System.currentTimeMillis() + random.nextInt(BusinessConstants.Random.APPROVAL_NUMBER_RANGE);
    }
    
    /**
     * 좌석 열 번호 생성 (01, 02, 03...)
     */
    public String generateSeatColumn(int columnNumber) {
        return String.format(BusinessConstants.Format.TWO_DIGIT_FORMAT, columnNumber);
    }
    
    /**
     * 상영관 ID 생성 (순차적)
     */
    public String generateScreenId(int screenNumber) {
        return String.format(BusinessConstants.Format.FOUR_DIGIT_FORMAT, screenNumber);
    }
    
    /**
     * 에러 코드 생성
     */
    public String generateErrorCode() {
        return "ERR" + random.nextInt(BusinessConstants.Random.ERROR_CODE_RANGE);
    }
} 