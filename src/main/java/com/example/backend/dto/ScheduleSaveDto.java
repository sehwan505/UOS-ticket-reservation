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
public class ScheduleSaveDto {

    @NotNull(message = "영화 정보는 필수입니다")
    private Long movieId;

    @NotBlank(message = "상영관 정보는 필수입니다")
    private String screenId;

    @NotBlank(message = "상영일은 필수입니다")
    @Pattern(regexp = "\\d{8}", message = "상영일은 YYYYMMDD 형식이어야 합니다")
    private String screeningDate;

    @NotBlank(message = "상영 시작 시간은 필수입니다")
    @Pattern(regexp = "\\d{4}", message = "시작 시간은 HHMM 형식이어야 합니다")
    private String screeningStartTime;

    // 상영일과 상영시작시간을 합친 전체 상영시간 문자열 반환
    public String getFullScreeningTime() {
        return screeningDate + screeningStartTime;
    }
}