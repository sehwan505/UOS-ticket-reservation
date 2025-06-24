package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Email;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationTransferDto {
    
    @NotEmpty(message = "전달할 예약 ID 목록은 필수입니다")
    private List<String> reservationIds;
    
    // 전달받을 사용자의 userId 또는 email 중 하나만 입력
    private String targetUserId;
    
    @Email(message = "유효한 이메일 형식이 아닙니다")
    private String targetEmail;
    
    // 전달 메시지 (선택사항)
    private String message;
} 