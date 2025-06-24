package com.example.backend.dto;

import lombok.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationCreateDto {
    
    @NotBlank(message = "스케줄 ID는 필수입니다.")
    private String scheduleId;
    
    @NotNull(message = "좌석 ID 목록은 필수입니다.")
    @NotEmpty(message = "최소 하나의 좌석을 선택해야 합니다.")
    private List<Integer> seatIds;
    
    // 비회원의 경우 필수, 회원의 경우 선택사항 (JWT에서 추출)
    private String phoneNumber;
    
    // 할인 관련 필드 제거됨 - 결제 시 적용
} 