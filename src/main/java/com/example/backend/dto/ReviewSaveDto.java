package com.example.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewSaveDto {
    private Long movieId;
    
    private String memberUserId;
    
    @NotNull(message = "평점은 필수입니다")
    @Min(value = 1, message = "평점은 최소 1점 이상이어야 합니다")
    @Max(value = 5, message = "평점은 최대 5점까지 가능합니다")
    private Integer ratingValue;
    
    @Size(max = 1000, message = "리뷰는 최대 1000자까지 작성 가능합니다")
    private String content;
}