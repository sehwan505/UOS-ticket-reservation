package sehwan505.uosticketreservation.service;

import sehwan505.uosticketreservation.dto.PointHistoryDto;
import sehwan505.uosticketreservation.entity.MemberEntity;
import sehwan505.uosticketreservation.entity.PointHistoryEntity;
import sehwan505.uosticketreservation.repository.MemberRepository;
import sehwan505.uosticketreservation.repository.PointHistoryRepository;
import sehwan505.uosticketreservation.constants.BusinessConstants;
import sehwan505.uosticketreservation.constants.StatusConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointHistoryService {

    private final PointHistoryRepository pointHistoryRepository;
    private final MemberRepository memberRepository;

    // 회원별 포인트 내역 조회 (페이징)
    public Page<PointHistoryDto> findPointHistoryByMember(String userId, Pageable pageable) {
        MemberEntity member = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. ID: " + userId));

        Page<PointHistoryEntity> pointHistoryPage = pointHistoryRepository.findByMember(member, pageable);
        return pointHistoryPage.map(this::convertToDto);
    }

    // 포인트 적립 내역 생성
    @Transactional
    public Long addPointHistory(String userId, Integer amount, String type) {
        MemberEntity member = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. ID: " + userId));

        // 포인트 내역 생성
        PointHistoryEntity pointHistory = PointHistoryEntity.builder()
                .member(member)
                .amount(amount)
                .type(type)
                .build();

        // 회원 포인트 업데이트
        updateMemberPoints(member, amount, type);

        PointHistoryEntity savedPointHistory = pointHistoryRepository.save(pointHistory);
        return savedPointHistory.getId();
    }

    // 회원 포인트 업데이트 (적립/사용/소멸)
    private void updateMemberPoints(MemberEntity member, Integer amount, String type) {
        int currentPoints = member.getAvailablePoints();

        switch (type) {
            case StatusConstants.PointHistory.ACCUMULATE -> // 적립
                    member.setAvailablePoints(currentPoints + amount);
            case StatusConstants.PointHistory.USE -> { // 사용
                if (currentPoints < amount) {
                    throw new IllegalArgumentException("사용 가능한 포인트가 부족합니다.");
                }
                member.setAvailablePoints(currentPoints - amount);
            }
            case StatusConstants.PointHistory.EXPIRE -> { // 소멸
                int pointsToExpire = Math.min(currentPoints, amount);
                member.setAvailablePoints(currentPoints - pointsToExpire);
            }
        }
    }

    // 예매 완료 후 포인트 적립
    @Transactional
    public Long addPointsForReservation(String userId, Integer reservationAmount) {
        // 예매 금액의 5% 포인트 적립
        Integer pointsToAdd = (int) Math.round(reservationAmount * BusinessConstants.Points.EARNING_RATE);
        
        // 최소 10포인트, 최대 1000포인트 적립
        pointsToAdd = Math.max(BusinessConstants.Points.MIN_POINTS, Math.min(BusinessConstants.Points.MAX_POINTS, pointsToAdd));
        
        return addPointHistory(userId, pointsToAdd, StatusConstants.PointHistory.ACCUMULATE);
    }

    // 회원 포인트 사용 처리
    @Transactional
    public Long usePoints(String userId, Integer pointsToUse) {
        if (pointsToUse <= 0) {
            throw new IllegalArgumentException("사용할 포인트는 0보다 커야 합니다.");
        }
        
        return addPointHistory(userId, pointsToUse, StatusConstants.PointHistory.USE);
    }

    // 회원 포인트 적립 처리
    @Transactional
    public Long addPoints(String userId, Integer pointsToAdd) {
        if (pointsToAdd <= 0) {
            throw new IllegalArgumentException("적립할 포인트는 0보다 커야 합니다.");
        }
        
        return addPointHistory(userId, pointsToAdd, StatusConstants.PointHistory.ACCUMULATE);
    }

    // Entity를 DTO로 변환
    private PointHistoryDto convertToDto(PointHistoryEntity pointHistory) {
        return PointHistoryDto.builder()
                .id(pointHistory.getId())
                .memberUserId(pointHistory.getMember().getUserId())
                .amount(pointHistory.getAmount())
                .type(pointHistory.getType())
                .build();
    }
}