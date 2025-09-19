package sehwan505.uosticketreservation.dto;

import sehwan505.uosticketreservation.constants.StatusConstants;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationDto {
    private String id;
    private String scheduleId;
    private String movieTitle;
    private String screenName;
    private String cinemaName;
    private Integer seatId;
    private String seatLabel;
    private String seatGradeName;
    private String status;
    private LocalDateTime reservationTime;
    private Integer basePrice;
    private Integer discountAmount;
    private Integer finalPrice;
    private String paymentId;
    private String paymentStatus;
    private String ticketIssuanceStatus;
    private String memberUserId;
    private String userName;
    private String phoneNumber;
    private String screeningDate;
    private LocalDateTime screeningStartTime;
    private String isTransferred;

    // 상태 텍스트 반환
    public String getStatusText() {
        return switch (status) {
            case StatusConstants.Reservation.NOT_COMPLETED -> "예매 미완료";
            case StatusConstants.Reservation.CANCELLED -> "예매 취소 중";
            case StatusConstants.Reservation.COMPLETED -> "예매 완료";
            default -> "알 수 없음";
        };
    }

    public String getTicketIssuanceStatusText() {
        return StatusConstants.Description.getTicketIssuanceStatus(ticketIssuanceStatus);
    }

    // 전달 상태 텍스트 반환
    public String getTransferStatusText() {
        return StatusConstants.Description.getTransferStatus(isTransferred);
    }

    // 예매 상태가 완료인지 확인
    public boolean isCompleted() {
        return StatusConstants.Reservation.COMPLETED.equals(status);
    }

    // 발권 가능한지 확인
    public boolean isTicketIssuable() {
        return StatusConstants.Reservation.COMPLETED.equals(status) && 
               !StatusConstants.TicketIssuance.ISSUED.equals(ticketIssuanceStatus);
    }

    // 취소 가능한지 확인
    public boolean isCancellable() {
        return !StatusConstants.TicketIssuance.ISSUED.equals(ticketIssuanceStatus) &&
               StatusConstants.Transfer.NOT_TRANSFERRED.equals(isTransferred);
    }
}