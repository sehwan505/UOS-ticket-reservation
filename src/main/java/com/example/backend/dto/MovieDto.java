package com.example.backend.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieDto {
    private Long id;
    private String title;
    private String genre;
    private String releaseDate;
    private String screeningStatus;
    private Integer runtime;
    private String actorName;
    private String directorName;
    private String distributorName;
    private String viewingGrade;
    private String description;
    private String image;
    private Double rating;
    
    // 상영 상태 텍스트 반환
    public String getScreeningStatusText() {
        return switch (screeningStatus) {
            case "N" -> "상영 예정";
            case "D" -> "상영중";
            case "Y" -> "상영 종료";
            default -> "알 수 없음";
        };
    }
    
    // 관람등급 텍스트 반환
    public String getViewingGradeText() {
        return switch (viewingGrade) {
            case "ALL" -> "전체 관람가";
            case "12" -> "12세 이상 관람가";
            case "15" -> "15세 이상 관람가";
            case "19" -> "청소년 관람불가";
            default -> viewingGrade;
        };
    }
}
