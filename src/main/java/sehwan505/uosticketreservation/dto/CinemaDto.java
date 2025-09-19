package sehwan505.uosticketreservation.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CinemaDto {
    private String id;
    private String name;
    private String location;
    private String regionId;
    private String regionName;
    private Integer screenCount;
} 