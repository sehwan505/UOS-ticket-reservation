package com.example.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovieSaveDto {
    @NotBlank(message = "영화 제목은 필수입니다")
    private String title;
    
    private String genre;
    
    @Pattern(regexp = "\\d{8}", message = "날짜는 YYYYMMDD 형식이어야 합니다")
    private String releaseDate;
    
    @Pattern(regexp = "[NDY]", message = "상영 상태는 N, D, Y 중 하나여야 합니다")
    private String screeningStatus;
    
    @NotNull(message = "상영 시간은 필수입니다")
    private Integer runtime;
    
    private String actorName;
    
    private String directorName;
    
    private String distributorName;
    
    private String viewingGrade;
    
    private String description;
    
    private String image;
}