package sehwan505.uosticketreservation.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberDto {
    private String userId;
    private String email;
    private String phoneNumber;
    private String birthDate;
    private String grade;
    private Integer availablePoints;
    
    // 회원 등급 텍스트 반환
    public String getGradeText() {
        return switch (grade) {
            case "B" -> "브론즈";
            case "S" -> "실버";
            case "G" -> "골드";
            case "P" -> "플래티넘";
            case "A" -> "관리자";
            default -> "일반";
        };
    }
}