package com.example.backend.controller;

import com.example.backend.dto.*;
import com.example.backend.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final ScheduleService scheduleService;
    private final MovieService movieService;
    private final SeatService seatService;
    private final PaymentService paymentService;
    private final MemberService memberService;
    private final BankService bankService;

    // 영화 예매 시작 페이지
    @GetMapping("/movies")
    public String selectMovie(Model model) {
        // 현재 상영중인 영화 목록
        List<MovieDto> nowShowingMovies = movieService.findMoviesByScreeningStatus("D", null).getContent();
        model.addAttribute("movies", nowShowingMovies);
        return "reservations/select_movie";
    }

    // 상영일 선택 페이지
    @GetMapping("/movies/{movieId}/dates")
    public String selectDate(@PathVariable Long movieId, Model model) {
        MovieDto movie = movieService.findMovieById(movieId);
        List<String> availableDates = scheduleService.findAvailableDatesForMovie(movieId);
        
        model.addAttribute("movie", movie);
        model.addAttribute("dates", availableDates);
        
        return "reservations/select_date";
    }

    // 상영시간 선택 페이지
    @GetMapping("/movies/{movieId}/dates/{date}")
    public String selectTime(@PathVariable Long movieId, 
                            @PathVariable String date, 
                            Model model) {
        
        MovieDto movie = movieService.findMovieById(movieId);
        List<ScheduleDto> schedules = scheduleService.findSchedulesByMovieAndDate(movieId, date);
        
        model.addAttribute("movie", movie);
        model.addAttribute("date", date);
        model.addAttribute("schedules", schedules);
        
        return "reservations/select_time";
    }

    // 좌석 선택 페이지
    @GetMapping("/schedules/{scheduleId}/seats")
    public String selectSeat(@PathVariable String scheduleId, Model model) {
        ScheduleDto schedule = scheduleService.findScheduleById(scheduleId);
        MovieDto movie = movieService.findMovieById(schedule.getMovieId());
        
        // 좌석 정보
        List<SeatDto> seats = seatService.findSeatsByScreen(schedule.getScreenId());
        
        // 이미 예약된 좌석 ID 목록
        List<Integer> reservedSeatIds = reservationService.findReservedSeatsBySchedule(scheduleId);
        
        model.addAttribute("schedule", schedule);
        model.addAttribute("movie", movie);
        model.addAttribute("seats", seats);
        model.addAttribute("reservedSeatIds", reservedSeatIds);
        
        return "reservations/select_seat";
    }

    // 결제 정보 입력 페이지
    @GetMapping("/confirm")
    public String confirmReservation(
            @RequestParam String scheduleId,
            @RequestParam Integer seatId,
            Model model) {
        
        // 스케줄 및 좌석 정보 조회
        ScheduleDto schedule = scheduleService.findScheduleById(scheduleId);
        SeatDto seat = seatService.findSeatById(seatId);
        MovieDto movie = movieService.findMovieById(schedule.getMovieId());
        
        // 로그인 회원 정보 가져오기
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        MemberDto member = null;
        
        if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
            member = memberService.findMemberByUserId(auth.getName());
            model.addAttribute("member", member);
        }
        
        model.addAttribute("schedule", schedule);
        model.addAttribute("seat", seat);
        model.addAttribute("movie", movie);
        model.addAttribute("paymentForm", new PaymentSaveDto());
        
        return "reservations/confirm";
    }

    // 예매 처리 (결제 포함)
    @PostMapping
    @ResponseBody
    public ResponseEntity<Map<String, Object>> processReservation(
            @RequestBody ReservationProcessDto processDto) {
        
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

    // 예매 완료 페이지
    @GetMapping("/complete/{reservationId}")
    public String reservationComplete(@PathVariable String reservationId, Model model) {
        ReservationDto reservation = reservationService.findReservationById(reservationId);
        model.addAttribute("reservation", reservation);
        return "reservations/complete";
    }

    // 회원 예매 내역 조회
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public String myReservations(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        MemberDto member = memberService.findMemberByUserId(auth.getName());
        
        List<ReservationDto> reservations = reservationService.findReservationsByMember(member.getId());
        model.addAttribute("reservations", reservations);
        
        return "reservations/my_reservations";
    }

    // 비회원 예매 내역 조회
    @GetMapping("/non-member")
    public String nonMemberReservationForm() {
        return "reservations/non_member_form";
    }

    // 비회원 예매 확인
    @PostMapping("/non-member/check")
    public String checkNonMemberReservation(
            @RequestParam String phoneNumber,
            @RequestParam String reservationId,
            Model model) {
        
        try {
            ReservationDto reservation = reservationService.findReservationById(reservationId);
            
            // 전화번호 확인
            if (reservation.getPhoneNumber() == null || !reservation.getPhoneNumber().equals(phoneNumber)) {
                model.addAttribute("error", "예약 정보가 일치하지 않습니다.");
                return "reservations/non_member_form";
            }
            
            model.addAttribute("reservation", reservation);
            return "reservations/non_member_detail";
            
        } catch (Exception e) {
            model.addAttribute("error", "존재하지 않는 예약입니다.");
            return "reservations/non_member_form";
        }
    }

    // 예매 취소
    @PostMapping("/{reservationId}/cancel")
    @ResponseBody
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

    // 티켓 발권
    @PostMapping("/{reservationId}/issue")
    @ResponseBody
    public ResponseEntity<Map<String, String>> issueTicket(@PathVariable String reservationId) {
        try {
            reservationService.issueTicket(reservationId);
            return ResponseEntity.ok(Map.of("message", "티켓이 발급되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}