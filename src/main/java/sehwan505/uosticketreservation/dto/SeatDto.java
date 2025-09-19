package sehwan505.uosticketreservation.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatDto {
    private Integer id;
    private String seatGradeId;
    private String seatGradeName;
    private String row;
    private String column;
    private String screenId;
    private Integer price;
    
    // 좌석 라벨 반환 (예: A01)
    public String getSeatLabel() {
        return row + column;
    }
}