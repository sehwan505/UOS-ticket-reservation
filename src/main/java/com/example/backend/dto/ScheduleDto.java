package com.example.backend.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleDto {
    private String id;
    private Long movieId;
    private String movieTitle;
    private String screenId;
    private String screenName;
    private String cinemaName;
    private String screeningDate;
    private LocalDateTime screeningStartTime;
    private Integer runtime;

    // 종료 시간 계산
    public LocalDateTime getScreeningEndTime() {
        if (screeningStartTime == null || runtime == null) {
            return null;
        }
        return screeningStartTime.plusMinutes(runtime);
    }
}