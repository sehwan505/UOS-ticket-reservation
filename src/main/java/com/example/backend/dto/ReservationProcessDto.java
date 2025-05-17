package com.example.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationProcessDto {
    private String scheduleId;
    private Integer seatId;
    private Long memberId;
    private String phoneNumber;
    private String discountCode;
    private Integer discountAmount;
    private String paymentMethod;
    private Integer amount;
    private Integer deductedPoints;
    private String cardOrAccountNumber;
}