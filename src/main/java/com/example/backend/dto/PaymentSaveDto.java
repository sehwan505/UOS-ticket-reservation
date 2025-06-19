package com.example.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentSaveDto {
    private String method;
    private Integer amount;
    private String memberUserId;
    private Integer deductedPoints;
}