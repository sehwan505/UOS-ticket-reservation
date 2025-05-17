package com.example.backend.dto;

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
    private Long memberId;
    private String userName;
    private String phoneNumber;
    private String screeningDate;
    private LocalDateTime screeningStartTime;

    // 상태 텍스트 반환
    public String getStatusText() {
        return switch (status) {
            case "N" -> "예매 중";
            case "D" -> "예매 취소 중";
            case "Y" -> "예매 완료";
            default -> "알 수 없음";
        };
    }

    public String getTicketIssuanceStatusText() {
        return "Y".equals(ticketIssuanceStatus) ? "발권 완료" : "미발권";
    }

    // 예매 상태가 완료인지 확인
    public boolean isCompleted() {
        return "Y".equals(status);
    }

    // 발권 가능한지 확인
    public boolean isTicketIssuable() {
        return "Y".equals(status) && !"Y".equals(ticketIssuanceStatus);
    }

    // 취소 가능한지 확인
    public boolean isCancellable() {
        return "Y".equals(status) && !"Y".equals(ticketIssuanceStatus);
    }
}