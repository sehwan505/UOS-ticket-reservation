package com.example.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScreenDto {
    private String id;
    private String name;
    private Integer totalSeats;
    private String cinemaId;
    private String cinemaName;
} 