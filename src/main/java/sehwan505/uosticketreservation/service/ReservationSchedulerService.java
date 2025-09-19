package sehwan505.uosticketreservation.service;

import sehwan505.uosticketreservation.dto.ReservationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationSchedulerService {
    
    private final ReservationService reservationService;
    
    // application.properties에서 설정할 수 있도록 값 주입
    // 기본값: 30분 (단위: 분)
    @Value("${reservation.payment.timeout:30}")
    private int paymentTimeoutMinutes;
    
    /**
     * 매 10분마다 미결제 예약을 체크하여 자동 취소
     * cron = 초 분 시 일 월 요일
     */
    @Scheduled(cron = "0 */10 * * * *")
    public void cancelExpiredReservations() {
        log.info("미결제 예약 자동 취소 작업 시작");
        
        try {
            List<String> canceledIds = reservationService.cancelExpiredReservations(paymentTimeoutMinutes);
            
            if (!canceledIds.isEmpty()) {
                log.info("자동 취소된 예약 수: {}, 예약 ID: {}", canceledIds.size(), canceledIds);
            } else {
                log.debug("취소할 만료된 예약이 없습니다.");
            }
            
        } catch (Exception e) {
            log.error("예약 자동 취소 작업 중 오류 발생", e);
        }
        
        log.info("미결제 예약 자동 취소 작업 완료");
    }
    
    /**
     * 매 1시간마다 미결제 예약 현황을 로깅
     * 모니터링 목적
     */
    @Scheduled(cron = "0 0 * * * *")
    public void logUnpaidReservationsStatus() {
        try {
            List<ReservationDto> unpaidReservations = reservationService.findUnpaidReservations();
            log.info("현재 미결제 예약 수: {}", unpaidReservations.size());
            
            if (unpaidReservations.size() > 10) {
                log.warn("미결제 예약이 많습니다. 시스템 점검이 필요할 수 있습니다.");
            }
            
        } catch (Exception e) {
            log.error("미결제 예약 현황 조회 중 오류 발생", e);
        }
    }
} 