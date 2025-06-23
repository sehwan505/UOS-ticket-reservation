package com.example.backend.dto;

import com.example.backend.constants.StatusConstants;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDto {
    private String id;
    private String method;
    private Integer deductedPoints;
    private Integer amount;
    private LocalDateTime paymentTime;
    private String status;
    private String approvalNumber;
    
    // 결제 상태 텍스트 반환
    public String getStatusText() {
        return switch (status) {
            case StatusConstants.Payment.NOT_COMPLETED -> "결제 미완료";
            case StatusConstants.Payment.PROCESSING -> "결제 중";
            case StatusConstants.Payment.COMPLETED -> "결제 완료";
            default -> "알 수 없음";
        };
    }
    
    // 결제 방식 텍스트 반환
    public String getMethodText() {
        if (method == null) {
            return "미지정";
        }
        
        if (method.startsWith("CARD")) {
            return "신용카드 (" + method.replace("CARD_COMPANY_", "") + ")";
        } else if (method.startsWith("BANK")) {
            return "계좌이체 (" + method.replace("BANK_COMPANY_", "") + ")";
        } else {
            return method;
        }
    }
}