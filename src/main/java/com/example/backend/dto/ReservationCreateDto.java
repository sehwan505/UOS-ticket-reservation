package com.example.backend.dto;

import lombok.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationCreateDto {
    
    @NotBlank(message = "스케줄 ID는 필수입니다.")
    private String scheduleId;
    
    @NotNull(message = "좌석 ID는 필수입니다.")
    private Integer seatId;
    
    // 비회원의 경우 필수, 회원의 경우 선택사항 (JWT에서 추출)
    private String phoneNumber;
    
    // 할인 관련 (선택사항)
    private String discountCode;
    private Integer discountAmount;
} 