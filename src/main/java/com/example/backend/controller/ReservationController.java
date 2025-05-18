package com.example.backend.controller;

import com.example.backend.dto.*;
import com.example.backend.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final ScheduleService scheduleService;
    private final MovieService movieService;
    private final SeatService seatService;
    private final PaymentService paymentService;
    private final MemberService memberService;
    private final BankService bankService;

    // 현재 상영중인 영화 목록 조회
    @GetMapping("/movies")
    public ResponseEntity<List<MovieDto>> getNowShowingMovies() {
        List<MovieDto> nowShowingMovies = movieService.findMoviesByScreeningStatus("D", null).getContent();
        return ResponseEntity.ok(nowShowingMovies);
    }

    // 영화별 상영 가능 날짜 조회
    @GetMapping("/movies/{movieId}/dates")
    public ResponseEntity<Map<String, Object>> getAvailableDates(@PathVariable Long movieId) {
        MovieDto movie = movieService.findMovieById(movieId);
        List<String> availableDates = scheduleService.findAvailableDatesForMovie(movieId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("movie", movie);
        response.put("dates", availableDates);
        
        return ResponseEntity.ok(response);
    }

    // 영화 및 날짜별 상영 스케줄 조회
    @GetMapping("/movies/{movieId}/dates/{date}")
    public ResponseEntity<Map<String, Object>> getSchedulesByDate(
            @PathVariable Long movieId, 
            @PathVariable String date) {
        
        MovieDto movie = movieService.findMovieById(movieId);
        List<ScheduleDto> schedules = scheduleService.findSchedulesByMovieAndDate(movieId, date);
        
        Map<String, Object> response = new HashMap<>();
        response.put("movie", movie);
        response.put("date", date);
        response.put("schedules", schedules);
        
        return ResponseEntity.ok(response);
    }

    // 스케줄별 좌석 정보 조회
    @GetMapping("/schedules/{scheduleId}/seats")
    public ResponseEntity<Map<String, Object>> getSeatsForSchedule(@PathVariable String scheduleId) {
        ScheduleDto schedule = scheduleService.findScheduleById(scheduleId);
        MovieDto movie = movieService.findMovieById(schedule.getMovieId());
        
        // 좌석 정보
        List<SeatDto> seats = seatService.findSeatsByScreen(schedule.getScreenId());
        
        // 이미 예약된 좌석 ID 목록
        List<Integer> reservedSeatIds = reservationService.findReservedSeatsBySchedule(scheduleId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("schedule", schedule);
        response.put("movie", movie);
        response.put("seats", seats);
        response.put("reservedSeatIds", reservedSeatIds);
        
        return ResponseEntity.ok(response);
    }

    // 예매 정보 조회
    @GetMapping("/confirm")
    public ResponseEntity<Map<String, Object>> getReservationInfo(
            @RequestParam String scheduleId,
            @RequestParam Integer seatId) {
        
        // 스케줄 및 좌석 정보 조회
        ScheduleDto schedule = scheduleService.findScheduleById(scheduleId);
        SeatDto seat = seatService.findSeatById(seatId);
        MovieDto movie = movieService.findMovieById(schedule.getMovieId());
        
        // 로그인 회원 정보 가져오기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        MemberDto member = null;
        
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            member = memberService.findMemberByUserId(auth.getName());
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("schedule", schedule);
        response.put("seat", seat);
        response.put("movie", movie);
        if (member != null) {
            response.put("member", member);
        }
        
        return ResponseEntity.ok(response);
    }

    // 예매 처리 (결제 포함)
    @PostMapping
    public ResponseEntity<Map<String, Object>> processReservation(
            @Valid @RequestBody ReservationProcessDto processDto) {
        
        try {
            // 1. 예매 정보 저장
            String reservationId = reservationService.saveReservation(
                    ReservationSaveDto.builder()
                            .scheduleId(processDto.getScheduleId())
                            .seatId(processDto.getSeatId())
                            .memberId(processDto.getMemberId())
                            .phoneNumber(processDto.getPhoneNumber())
                            .discountCode(processDto.getDiscountCode())
                            .discountAmount(processDto.getDiscountAmount())
                            .build()
            );
            
            // 2. 결제 정보 저장
            String paymentId = paymentService.savePayment(
                    PaymentSaveDto.builder()
                            .method(processDto.getPaymentMethod())
                            .amount(processDto.getAmount())
                            .memberId(processDto.getMemberId())
                            .deductedPoints(processDto.getDeductedPoints())
                            .build()
            );
            
            // 3. 가상의 은행/카드사 통신
            Map<String, Object> paymentResult = bankService.requestPaymentApproval(
                    processDto.getPaymentMethod(),
                    processDto.getAmount(),
                    processDto.getCardOrAccountNumber()
            );
            
            if ("SUCCESS".equals(paymentResult.get("status"))) {
                // 4. 결제 완료 처리
                paymentService.completePayment(paymentId);
                
                // 5. 예매 완료 처리
                reservationService.completeReservation(reservationId, paymentId);
                
                paymentResult.put("reservationId", reservationId);
                return ResponseEntity.ok(paymentResult);
            } else {
                // 결제 실패 시 예매 정보 취소
                reservationService.cancelReservation(reservationId);
                paymentService.cancelPayment(paymentId);
                
                return ResponseEntity.badRequest().body(paymentResult);
            }
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "FAIL",
                    "message", e.getMessage()
            ));
        }
    }

    // 예매 상세 정보 조회
    @GetMapping("/{reservationId}")
    public ResponseEntity<ReservationDto> getReservationDetail(@PathVariable String reservationId) {
        ReservationDto reservation = reservationService.findReservationById(reservationId);
        return ResponseEntity.ok(reservation);
    }

    // 회원 예매 내역 조회
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ReservationDto>> getMyReservations() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        MemberDto member = memberService.findMemberByUserId(auth.getName());
        
        List<ReservationDto> reservations = reservationService.findReservationsByMember(member.getId());
        return ResponseEntity.ok(reservations);
    }

    // 비회원 예매 내역 조회
    @GetMapping("/non-member/check")
    public ResponseEntity<?> getNonMemberReservation(
            @RequestParam String phoneNumber,
            @RequestParam String reservationId) {
        
        try {
            ReservationDto reservation = reservationService.findReservationById(reservationId);
            
            // 전화번호 확인
            if (reservation.getPhoneNumber() == null || !reservation.getPhoneNumber().equals(phoneNumber)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "예약 정보가 일치하지 않습니다."));
            }
            
            return ResponseEntity.ok(reservation);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "존재하지 않는 예약입니다."));
        }
    }

    // 예매 취소
    @DeleteMapping("/{reservationId}")
    public ResponseEntity<Map<String, Object>> cancelReservation(@PathVariable String reservationId) {
        try {
            ReservationDto reservation = reservationService.findReservationById(reservationId);
            
            // 결제 취소 요청 (은행/카드사 통신)
            String approvalNumber = reservation.getPaymentId();
            Map<String, Object> cancelResult = bankService.requestPaymentCancellation(approvalNumber);
            
            if ("SUCCESS".equals(cancelResult.get("status"))) {
                // 결제 취소 처리
                paymentService.cancelPayment(reservation.getPaymentId());
                
                // 예약 취소 처리
                reservationService.cancelReservation(reservationId);
                
                return ResponseEntity.ok(Map.of(
                        "status", "SUCCESS",
                        "message", "예매가 취소되었습니다."
                ));
            } else {
                return ResponseEntity.badRequest().body(cancelResult);
            }
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "FAIL",
                    "message", e.getMessage()
            ));
        }
    }

    // 티켓 발급
    @PostMapping("/{reservationId}/issue")
    public ResponseEntity<Map<String, String>> issueTicket(@PathVariable String reservationId) {
        try {
            String ticketUrl = reservationService.issueTicket(reservationId);
            return ResponseEntity.ok(Map.of(
                    "ticketUrl", ticketUrl,
                    "message", "티켓이 발급되었습니다."
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage()
            ));
        }
    }
}