package com.example.backend.service;

import com.example.backend.dto.ReservationDto;
import com.example.backend.dto.ReservationSaveDto;
import com.example.backend.entity.*;
import com.example.backend.repository.*;
import lombok.RequiredArgsConstructor;
import com.example.backend.constants.StatusConstants;
import com.example.backend.constants.BusinessConstants;
import com.example.backend.util.IdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.dao.DataIntegrityViolationException;

import jakarta.persistence.PessimisticLockException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ReservationService {
    
    private final ReservationRepository reservationRepository;
    private final ScheduleRepository scheduleRepository;
    private final SeatRepository seatRepository;
    private final SeatGradeRepository seatGradeRepository;
    private final MemberRepository memberRepository;
    private final NonMemberRepository nonMemberRepository;
    private final PaymentRepository paymentRepository;
    private final IdGenerator idGenerator;
    
    // 모든 예매 조회
    public List<ReservationDto> findAllReservations() {
        return reservationRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    // 예매 상세 조회
    public ReservationDto findReservationById(String id) {
        ReservationEntity reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예매입니다. ID: " + id));
        
        return convertToDto(reservation);
    }
    
    // 회원별 예매 조회
    public List<ReservationDto> findReservationsByMember(String userId) {
        MemberEntity member = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. ID: " + userId));
        
        return reservationRepository.findByMember(member).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    // 비회원 예매 조회
    public List<ReservationDto> findReservationsByNonMember(String phoneNumber) {
        NonMemberEntity nonMember = nonMemberRepository.findById(phoneNumber)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 비회원입니다. 전화번호: " + phoneNumber));
        
        return reservationRepository.findByNonMember(nonMember).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    // 상영일정별 예매된 좌석 조회
    public List<Integer> findReservedSeatsBySchedule(String scheduleId) {
        return reservationRepository.findReservedSeatIdsByScheduleId(scheduleId);
    }
    
    // 예매 등록 (중복 체크 로직 추가)
    @Transactional(isolation = Isolation.READ_COMMITTED, timeout = BusinessConstants.Transaction.RESERVATION_TIMEOUT_SECONDS)
    public String saveReservation(ReservationSaveDto reservationSaveDto) {
        log.info("예약 생성 시작: 스케줄={}, 좌석={}", reservationSaveDto.getScheduleId(), reservationSaveDto.getSeatId());
        
        try {
            // 1. 기본 엔티티 조회
            ScheduleEntity schedule = scheduleRepository.findById(reservationSaveDto.getScheduleId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상영일정입니다. ID: " + reservationSaveDto.getScheduleId()));
            
            SeatEntity seat = seatRepository.findById(reservationSaveDto.getSeatId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 좌석입니다. ID: " + reservationSaveDto.getSeatId()));
            
            // 2. 배타적 락을 사용한 중복 체크
            Optional<ReservationEntity> existingReservation = reservationRepository
                    .findActiveReservationByScheduleAndSeatWithLock(
                        reservationSaveDto.getScheduleId(), 
                        reservationSaveDto.getSeatId()
                    );
            
            if (existingReservation.isPresent()) {
                log.warn("중복 예약 시도: 스케줄={}, 좌석={}", reservationSaveDto.getScheduleId(), reservationSaveDto.getSeatId());
                throw new IllegalStateException("이미 예약된 좌석입니다. 다른 좌석을 선택해주세요.");
            }
            
            // 3. 예매 생성
            SeatGradeEntity seatGrade = seat.getSeatGrade();
            
            // 예매 ID 생성
            int dailyReservationCount = reservationRepository.countCompletedReservationsByScheduleId(schedule.getId()) + 1;
            String reservationId = idGenerator.generateReservationId(schedule.getId(), seat.getId().toString(), dailyReservationCount);
            
            // 예매 엔티티 생성
            ReservationEntity reservation = ReservationEntity.builder()
                    .id(reservationId)
                    .schedule(schedule)
                    .seat(seat)
                    .seatGrade(seatGrade)
                    .status(StatusConstants.Reservation.NOT_COMPLETED) // 예매미완료 상태
                    .reservationTime(LocalDateTime.now())
                    .basePrice(seatGrade.getPrice())
                    .discountAmount(BusinessConstants.Points.INITIAL_POINTS)
                    .ticketIssuanceStatus(StatusConstants.TicketIssuance.NOT_ISSUED)
                    .build();
            
            // 할인 적용
            if (reservationSaveDto.getDiscountCode() != null) {
                reservation.applyDiscount(reservationSaveDto.getDiscountCode(), reservationSaveDto.getDiscountAmount());
            } else {
                reservation.setFinalPrice(seatGrade.getPrice());
            }
            
            // 회원/비회원 정보 설정
            setReservationUser(reservation, reservationSaveDto);
            
            // 저장
            ReservationEntity savedReservation = reservationRepository.save(reservation);
            log.info("예약 생성 완료: {}", savedReservation.getId());
            
            return savedReservation.getId();
            
        } catch (PessimisticLockException e) {
            log.error("락 획득 실패: 스케줄={}, 좌석={}", reservationSaveDto.getScheduleId(), reservationSaveDto.getSeatId());
            throw new IllegalStateException("다른 사용자가 같은 좌석을 선택 중입니다. 잠시 후 다시 시도해주세요.", e);
        } catch (DataIntegrityViolationException e) {
            log.error("데이터 무결성 위반: 스케줄={}, 좌석={}", reservationSaveDto.getScheduleId(), reservationSaveDto.getSeatId());
            throw new IllegalStateException("좌석 예약 중 충돌이 발생했습니다. 다시 시도해주세요.", e);
        } catch (Exception e) {
            log.error("예약 생성 중 오류: {}", e.getMessage(), e);
            throw new RuntimeException("예약 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    // 예매 결제 완료 처리
    @Transactional
    public String completeReservation(String reservationId, String paymentId) {
        ReservationEntity reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예매입니다. ID: " + reservationId));

        PaymentEntity payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 결제입니다. ID: " + paymentId));

        // 예매 상태 업데이트
        reservation.setStatus(StatusConstants.Reservation.COMPLETED); // 예매완료로 변경
        reservation.setPayment(payment);

        return reservation.getId();
    }

    // 예매 취소
    @Transactional
    public String cancelReservation(String reservationId) {
        ReservationEntity reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예매입니다. ID: " + reservationId));

        // 이미 발권된 티켓은 취소할 수 없음
        if (StatusConstants.TicketIssuance.ISSUED.equals(reservation.getTicketIssuanceStatus())) {
            throw new IllegalStateException("이미 발권된 티켓은 취소할 수 없습니다.");
        }

        // 예매 상태를 취소로 변경
        reservation.setStatus(StatusConstants.Reservation.CANCELLED); // 예매취소중으로 변경

        return reservation.getId();
    }

    // 티켓 발권 처리
    @Transactional
    public String issueTicket(String reservationId) {
        ReservationEntity reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예매입니다. ID: " + reservationId));

        // 예매가 완료된 경우만 발권 가능
        if (!StatusConstants.Reservation.COMPLETED.equals(reservation.getStatus())) {
            throw new IllegalStateException("예매가 완료되지 않아 발권할 수 없습니다.");
        }

        // 티켓 발권 상태로 변경
        reservation.setTicketIssuanceStatus(StatusConstants.TicketIssuance.ISSUED); // 발권으로 변경

        return reservation.getId();
    }

    // 자동 예약 취소를 위한 메서드 추가
    @Transactional
    public List<String> cancelExpiredReservations(int timeoutMinutes) {
        LocalDateTime cutoffTime = LocalDateTime.now().minusMinutes(timeoutMinutes);
        List<ReservationEntity> expiredReservations = reservationRepository.findUnpaidReservationsOlderThan(cutoffTime);
        
        List<String> canceledReservationIds = new ArrayList<>();
        
        for (ReservationEntity reservation : expiredReservations) {
            try {
                // 이미 발권된 티켓은 취소하지 않음
                if (!StatusConstants.TicketIssuance.ISSUED.equals(reservation.getTicketIssuanceStatus())) {
                    reservation.setStatus(StatusConstants.Reservation.CANCELLED); // 예매취소중으로 변경
                    canceledReservationIds.add(reservation.getId());
                    System.out.println("자동 취소된 예약: " + reservation.getId() + 
                                     ", 예약 시간: " + reservation.getReservationTime());
                }
            } catch (Exception e) {
                System.err.println("예약 취소 중 오류 발생: " + reservation.getId() + " - " + e.getMessage());
            }
        }
        
        return canceledReservationIds;
    }
    
    // 미결제 예약 현황 조회
    public List<ReservationDto> findUnpaidReservations() {
        return reservationRepository.findAllUnpaidReservations().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 실시간 좌석 상태 조회 (락 포함)
    @Transactional(readOnly = true, timeout = BusinessConstants.Transaction.READ_TIMEOUT_SECONDS)
    public List<Integer> getActiveReservedSeatsWithLock(String scheduleId) {
        try {
            return reservationRepository.findActiveReservedSeatIdsByScheduleIdWithLock(scheduleId);
        } catch (PessimisticLockException e) {
            log.warn("좌석 상태 조회 중 락 경합: {}", scheduleId);
            // 락 실패시 일반 조회로 폴백
            return reservationRepository.findReservedSeatIdsByScheduleId(scheduleId);
        }
    }

    // 좌석 예약 가능 여부 실시간 체크
    public boolean isSeatAvailable(String scheduleId, Integer seatId) {
        return !reservationRepository.existsActiveReservationByScheduleAndSeat(scheduleId, seatId);
    }

    // 회원/비회원 정보 설정 헬퍼 메서드
    private void setReservationUser(ReservationEntity reservation, ReservationSaveDto reservationSaveDto) {
        if (reservationSaveDto.getMemberUserId() != null) {
            MemberEntity member = memberRepository.findById(reservationSaveDto.getMemberUserId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. ID: " + reservationSaveDto.getMemberUserId()));
            reservation.setMember(member);
        } else if (reservationSaveDto.getPhoneNumber() != null) {
            NonMemberEntity nonMember = nonMemberRepository.findById(reservationSaveDto.getPhoneNumber())
                    .orElseGet(() -> {
                        NonMemberEntity newNonMember = NonMemberEntity.builder()
                                .phoneNumber(reservationSaveDto.getPhoneNumber())
                                .build();
                        return nonMemberRepository.save(newNonMember);
                    });
            reservation.setNonMember(nonMember);
        } else {
            throw new IllegalArgumentException("회원 ID 또는 비회원 전화번호 중 하나는 필수입니다.");
        }
    }

    // Entity를 DTO로 변환
    private ReservationDto convertToDto(ReservationEntity reservation) {
        ReservationDto dto = ReservationDto.builder()
                .id(reservation.getId())
                .scheduleId(reservation.getSchedule().getId())
                .movieTitle(reservation.getSchedule().getMovie().getTitle())
                .screenName(reservation.getSchedule().getScreen().getName())
                .cinemaName(reservation.getSchedule().getScreen().getCinema().getName())
                .seatId(reservation.getSeat().getId())
                .seatLabel(reservation.getSeat().getSeatLabel())
                .seatGradeName(reservation.getSeatGrade().getName())
                .status(reservation.getStatus())
                .reservationTime(reservation.getReservationTime())
                .basePrice(reservation.getBasePrice())
                .discountAmount(reservation.getDiscountAmount())
                .finalPrice(reservation.getFinalPrice())
                .ticketIssuanceStatus(reservation.getTicketIssuanceStatus())
                .screeningDate(reservation.getSchedule().getScreeningDate())
                .screeningStartTime(reservation.getSchedule().getScreeningStartTime())
                .build();

        // 회원 정보 설정
        if (reservation.getMember() != null) {
            dto.setMemberUserId(reservation.getMember().getUserId());
            dto.setUserName(reservation.getMember().getUserId());
        }

        // 비회원 정보 설정
        if (reservation.getNonMember() != null) {
            System.out.println("DEBUG: Converting to DTO - nonMember phoneNumber = " + reservation.getNonMember().getPhoneNumber());
            dto.setPhoneNumber(reservation.getNonMember().getPhoneNumber());
        } else {
            System.out.println("DEBUG: Converting to DTO - nonMember is null");
        }

        // 결제 정보 설정
        if (reservation.getPayment() != null) {
            dto.setPaymentId(reservation.getPayment().getId());
            dto.setPaymentStatus(reservation.getPayment().getStatus());
        }

        return dto;
    }

    // 예약 소유권 변경 (예약 전달)
    @Transactional
    public String transferReservation(String reservationId, String targetUserId) {
        ReservationEntity reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예매입니다. ID: " + reservationId));
        
        // 대상 회원 확인
        MemberEntity targetMember = memberRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("전달받을 회원이 존재하지 않습니다. ID: " + targetUserId));
        
        // 예약 상태 확인 (완료된 예약만 전달 가능)
        if (!StatusConstants.Reservation.COMPLETED.equals(reservation.getStatus())) {
            throw new IllegalArgumentException("완료된 예약만 전달할 수 있습니다.");
        }
        
        // 기존 회원/비회원 정보 제거
        reservation.setMember(null);
        reservation.setNonMember(null);
        
        // 새로운 회원으로 설정
        reservation.setMember(targetMember);
        
        return reservation.getId();
    }
    
    // 여러 예약 일괄 전달
    @Transactional
    public List<String> transferMultipleReservations(List<String> reservationIds, String targetUserId) {
        // 대상 회원 확인
        MemberEntity targetMember = memberRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("전달받을 회원이 존재하지 않습니다. ID: " + targetUserId));
        
        List<String> transferredIds = new ArrayList<>();
        
        for (String reservationId : reservationIds) {
            try {
                transferReservation(reservationId, targetUserId);
                transferredIds.add(reservationId);
            } catch (Exception e) {
                // 일부 예약 전달이 실패해도 다른 예약들은 계속 처리
                System.out.println("예약 전달 실패: " + reservationId + " - " + e.getMessage());
            }
        }
        
        return transferredIds;
    }

    // 예매에 할인 코드 적용
    @Transactional
    public void applyDiscountToReservation(String reservationId, String discountCode, Integer discountAmount) {
        ReservationEntity reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예매입니다. ID: " + reservationId));
        
        // 할인 적용
        reservation.applyDiscount(discountCode, discountAmount);

        log.info("예약 {}에 할인 적용: 코드={}, 금액={}", reservationId, discountCode, discountAmount);
    }
}