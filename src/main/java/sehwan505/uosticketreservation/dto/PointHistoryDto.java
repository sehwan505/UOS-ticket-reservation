package sehwan505.uosticketreservation.dto;

import sehwan505.uosticketreservation.constants.StatusConstants;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointHistoryDto {
    private Long id;
    private String memberUserId;
    private Integer amount;
    private String type;
    
    // 포인트 타입 텍스트 반환
    public String getTypeText() {
        return StatusConstants.Description.getPointHistoryStatus(type);
    }
    
    // 적립인지 확인
    public boolean isAccumulation() {
        return StatusConstants.PointHistory.ACCUMULATE.equals(type);
    }
    
    // 사용인지 확인
    public boolean isUsage() {
        return StatusConstants.PointHistory.USE.equals(type);
    }
    
    // 소멸인지 확인
    public boolean isExpiration() {
        return StatusConstants.PointHistory.EXPIRE.equals(type);
    }
}