package com.example.backend.service;

import com.example.backend.dto.ReservationDto;
import com.example.backend.dto.ReservationSaveDto;
import com.example.backend.entity.*;
import com.example.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReservationService {
    
    private final ReservationRepository reservationRepository;
    private final ScheduleRepository scheduleRepository;
    private final SeatRepository seatRepository;
    private final SeatGradeRepository seatGradeRepository;
    private final MemberRepository memberRepository;
    private final NonMemberRepository nonMemberRepository;
    private final PaymentRepository paymentRepository;
    
    // 모든 예매 조회
    public List<ReservationDto> findAllReservations() {
        return reservationRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    // 예매 상세 조회
    public ReservationDto findReservationById(String id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예매입니다. ID: " + id));
        
        return convertToDto(reservation);
    }
    
    // 회원별 예매 조회
    public List<ReservationDto> findReservationsByMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. ID: " + memberId));
        
        return reservationRepository.findByMember(member).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    // 비회원 예매 조회
    public List<ReservationDto> findReservationsByNonMember(String phoneNumber) {
        NonMember nonMember = nonMemberRepository.findById(phoneNumber)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 비회원입니다. 전화번호: " + phoneNumber));
        
        return reservationRepository.findByNonMember(nonMember).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    // 상영일정별 예매된 좌석 조회
    public List<Integer> findReservedSeatsBySchedule(String scheduleId) {
        return reservationRepository.findReservedSeatIdsByScheduleId(scheduleId);
    }
    
    // 예매 등록
    @Transactional
    public String saveReservation(ReservationSaveDto reservationSaveDto) {
        // 필요한 엔티티 조회
        Schedule schedule = scheduleRepository.findById(reservationSaveDto.getScheduleId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상영일정입니다. ID: " + reservationSaveDto.getScheduleId()));
        
        Seat seat = seatRepository.findById(reservationSaveDto.getSeatId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 좌석입니다. ID: " + reservationSaveDto.getSeatId()));
        
        SeatGrade seatGrade = seat.getSeatGrade();
        
        // 예매 ID 생성: 상영시간표번호 + 좌석번호 + 일일좌석예매횟수
        int dailyReservationCount = reservationRepository.countCompletedReservationsByScheduleId(schedule.getId()) + 1;
        String reservationId = schedule.getId() + seat.getId() + String.format("%02d", dailyReservationCount);
        
        // 예매 기본 정보 설정
        Reservation reservation = Reservation.builder()
                .id(reservationId)
                .schedule(schedule)
                .seat(seat)
                .seatGrade(seatGrade)
                .status("N") // 예매미완료 상태로 시작
                .reservationTime(LocalDateTime.now())
                .basePrice(seatGrade.getPrice())
                .discountAmount(0)
                .ticketIssuanceStatus("N") // 미발권 상태로 시작
                .build();
        
        // 할인 코드가 있으면 적용
        if (reservationSaveDto.getDiscountCode() != null) {
            reservation.applyDiscount(reservationSaveDto.getDiscountCode(), reservationSaveDto.getDiscountAmount());
        } else {
            // 할인 없는 경우 기본가격을 최종가격으로 설정
            reservation.setFinalPrice(seatGrade.getPrice());
        }
        
        // 회원 또는 비회원 정보 설정
        if (reservationSaveDto.getMemberId() != null) {
            Member member = memberRepository.findById(reservationSaveDto.getMemberId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. ID: " + reservationSaveDto.getMemberId()));
                    reservation.setMember(member);
        } else if (reservationSaveDto.getPhoneNumber() != null) {
            NonMember nonMember = nonMemberRepository.findById(reservationSaveDto.getPhoneNumber())
                    .orElseGet(() -> {
                        // 비회원이 없으면 새로 생성
                        NonMember newNonMember = NonMember.builder()
                                .phoneNumber(reservationSaveDto.getPhoneNumber())
                                .build();
                        return nonMemberRepository.save(newNonMember);
                    });
            reservation.setNonMember(nonMember);
        } else {
            throw new IllegalArgumentException("회원 ID 또는 비회원 전화번호 중 하나는 필수입니다.");
        }

        Reservation savedReservation = reservationRepository.save(reservation);
        return savedReservation.getId();
    }

    // 예매 결제 완료 처리
    @Transactional
    public String completeReservation(String reservationId, String paymentId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예매입니다. ID: " + reservationId));

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 결제입니다. ID: " + paymentId));

        // 예매 상태 업데이트
        reservation.setStatus("Y"); // 예매완료로 변경
        reservation.setPayment(payment);

        return reservation.getId();
    }

    // 예매 취소
    @Transactional
    public String cancelReservation(String reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예매입니다. ID: " + reservationId));

        // 이미 발권된 티켓은 취소할 수 없음
        if ("Y".equals(reservation.getTicketIssuanceStatus())) {
            throw new IllegalStateException("이미 발권된 티켓은 취소할 수 없습니다.");
        }

        // 예매 상태를 취소로 변경
        reservation.setStatus("D"); // 예매취소중으로 변경

        return reservation.getId();
    }

    // 티켓 발권 처리
    @Transactional
    public String issueTicket(String reservationId) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 예매입니다. ID: " + reservationId));

        // 예매가 완료된 경우만 발권 가능
        if (!"Y".equals(reservation.getStatus())) {
            throw new IllegalStateException("예매가 완료되지 않아 발권할 수 없습니다.");
        }

        // 티켓 발권 상태로 변경
        reservation.setTicketIssuanceStatus("Y"); // 발권으로 변경

        return reservation.getId();
    }

    // Entity를 DTO로 변환
    private ReservationDto convertToDto(Reservation reservation) {
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
            dto.setMemberId(reservation.getMember().getId());
            dto.setUserName(reservation.getMember().getUserId());
        }

        // 비회원 정보 설정
        if (reservation.getNonMember() != null) {
            dto.setPhoneNumber(reservation.getNonMember().getPhoneNumber());
        }

        // 결제 정보 설정
        if (reservation.getPayment() != null) {
            dto.setPaymentId(reservation.getPayment().getId());
            dto.setPaymentStatus(reservation.getPayment().getStatus());
        }

        return dto;
    }
}