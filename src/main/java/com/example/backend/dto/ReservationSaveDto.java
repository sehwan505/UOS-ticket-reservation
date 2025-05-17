package com.example.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationSaveDto {
    private String scheduleId;
    private Integer seatId;
    private Long memberId;
    private String phoneNumber;
    private String discountCode;
    private Integer discountAmount;
}