package sehwan505.uosticketreservation.dto;

import lombok.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Min;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentProcessDto {
    
    @NotNull(message = "예약 ID 목록은 필수입니다.")
    @NotEmpty(message = "최소 하나의 예약을 선택해야 합니다.")
    private List<String> reservationIds;
    
    @NotBlank(message = "결제 방법은 필수입니다.")
    private String paymentMethod;
    
    @NotNull(message = "결제 금액은 필수입니다.")
    @Min(value = 1, message = "결제 금액은 1원 이상이어야 합니다.")
    private Integer amount;
    
    @NotBlank(message = "카드번호 또는 계좌번호는 필수입니다.")
    private String cardOrAccountNumber;
    
    // 포인트 사용 (선택사항)
    @Min(value = 0, message = "사용 포인트는 0 이상이어야 합니다.")
    @Builder.Default
    private Integer deductedPoints = 0;
    
    // 할인 관련 필드 추가
    private String discountCode;
    private Integer discountAmount;
} 