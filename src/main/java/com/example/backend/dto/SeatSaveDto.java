package com.example.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatSaveDto {
    private Integer id;
    private String seatGradeId;
    private String row;
    private String column;
    private String screenId;
} 