package com.example.backend.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointHistoryDto {
    private Long id;
    private Long memberId;
    private Integer amount;
    private String type;
    private LocalDateTime pointTime;
    
    // 포인트 타입 텍스트 반환
    public String getTypeText() {
        return switch (type) {
            case "A" -> "적립";
            case "U" -> "사용";
            case "E" -> "소멸";
            default -> "알 수 없음";
        };
    }
    
    // 적립인지 확인
    public boolean isAccumulation() {
        return "A".equals(type);
    }
    
    // 사용인지 확인
    public boolean isUsage() {
        return "U".equals(type);
    }
    
    // 소멸인지 확인
    public boolean isExpiration() {
        return "E".equals(type);
    }
}