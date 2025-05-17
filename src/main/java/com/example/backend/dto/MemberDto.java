package com.example.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberDto {
    private Long id;
    private String userId;
    private String email;
    private String phoneNumber;
    private String birthDate;
    private String grade;
    private Integer availablePoints;
    
    // 회원 등급 텍스트 반환
    public String getGradeText() {
        return switch (grade) {
            case "1" -> "일반";
            case "2" -> "실버";
            case "3" -> "골드";
            case "4" -> "VIP";
            case "9" -> "관리자";
            default -> "일반";
        };
    }
}