package com.example.backend.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDto {
    private Long id;
    private Long movieId;
    private String movieTitle;
    private String memberUserId;
    private Integer ratingValue;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 별점을 시각적으로 표현
    public String getRatingStars() {
        if (ratingValue == null) {
            return "";
        }
        
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < ratingValue; i++) {
            stars.append("★");
        }
        for (int i = ratingValue; i < 5; i++) {
            stars.append("☆");
        }
        return stars.toString();
    }
}