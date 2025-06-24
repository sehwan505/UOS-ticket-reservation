package com.example.backend.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NonMemberWithReservationsDto {
    private String phoneNumber;
    private List<ReservationDto> reservations;
    private int totalReservations;
    private int completedReservations;
    private int cancelledReservations;
    
    // 생성자에서 자동으로 통계 계산
    public NonMemberWithReservationsDto(String phoneNumber, List<ReservationDto> reservations) {
        this.phoneNumber = phoneNumber;
        this.reservations = reservations;
        this.totalReservations = reservations.size();
        this.completedReservations = (int) reservations.stream()
                .filter(r -> "Y".equals(r.getStatus()))
                .count();
        this.cancelledReservations = (int) reservations.stream()
                .filter(r -> "D".equals(r.getStatus()))
                .count();
    }
} 