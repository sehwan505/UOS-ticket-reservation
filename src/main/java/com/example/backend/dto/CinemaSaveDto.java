package com.example.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CinemaSaveDto {
    private String id;
    private String name;
    private String location;
    private String regionId;
} 