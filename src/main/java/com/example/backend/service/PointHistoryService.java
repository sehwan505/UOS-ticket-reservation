package com.example.backend.service;

import com.example.backend.dto.PointHistoryDto;
import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.PointHistoryEntity;
import com.example.backend.repository.MemberRepository;
import com.example.backend.repository.PointHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PointHistoryService {

    private final PointHistoryRepository pointHistoryRepository;
    private final MemberRepository memberRepository;

    // 회원별 포인트 내역 조회 (페이징)
    public Page<PointHistoryDto> findPointHistoryByMember(Long memberId, Pageable pageable) {
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. ID: " + memberId));

        return pointHistoryRepository.findByMember(member, pageable)
                .map(this::convertToDto);
    }

    // 포인트 적립 내역 생성
    @Transactional
    public Long addPointHistory(Long memberId, Integer amount, String type) {
        MemberEntity member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. ID: " + memberId));

        // 포인트 내역 생성
        PointHistoryEntity pointHistory = PointHistoryEntity.builder()
                .member(member)
                .amount(amount)
                .type(type)
                .pointTime(LocalDateTime.now())
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
            case "A" -> // 적립
                    member.setAvailablePoints(currentPoints + amount);
            case "U" -> { // 사용
                if (currentPoints < amount) {
                    throw new IllegalArgumentException("사용 가능한 포인트가 부족합니다.");
                }
                member.setAvailablePoints(currentPoints - amount);
            }
            case "E" -> { // 소멸
                int pointsToExpire = Math.min(currentPoints, amount);
                member.setAvailablePoints(currentPoints - pointsToExpire);
            }
        }
    }

    // 예매 완료 후 포인트 적립
    @Transactional
    public Long addPointsForReservation(Long memberId, Integer reservationAmount) {
        // 결제 금액의 약 10%를 포인트로 적립 (소수점 버림)
        int pointsToAdd = (int) (reservationAmount * 0.1);

        if (pointsToAdd > 0) {
            return addPointHistory(memberId, pointsToAdd, "A");
        }

        return null;
    }

    // 회원 포인트 사용 처리
    @Transactional
    public Long usePoints(Long memberId, Integer pointsToUse) {
        if (pointsToUse <= 0) {
            throw new IllegalArgumentException("사용할 포인트는 1점 이상이어야 합니다.");
        }

        return addPointHistory(memberId, pointsToUse, "U");
    }

    // 회원 포인트 적립 처리
    @Transactional
    public Long addPoints(Long memberId, Integer pointsToAdd) {
        if (pointsToAdd <= 0) {
            throw new IllegalArgumentException("적립할 포인트는 1점 이상이어야 합니다.");
        }

        return addPointHistory(memberId, pointsToAdd, "A");
    }

    // Entity를 DTO로 변환
    private PointHistoryDto convertToDto(PointHistoryEntity pointHistory) {
        return PointHistoryDto.builder()
                .id(pointHistory.getId())
                .memberId(pointHistory.getMember().getId())
                .amount(pointHistory.getAmount())
                .type(pointHistory.getType())
                .pointTime(pointHistory.getPointTime())
                .build();
    }
}