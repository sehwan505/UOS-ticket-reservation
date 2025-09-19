package sehwan505.uosticketreservation.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationSaveDto {
    private String scheduleId;
    private Integer seatId;
    private String memberUserId;
    private String phoneNumber;
    private String discountCode;
    private Integer discountAmount;
}