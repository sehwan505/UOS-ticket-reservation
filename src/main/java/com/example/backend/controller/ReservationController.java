package com.example.backend.controller;

import com.example.backend.dto.*;
import com.example.backend.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
@Tag(name = "Reservation API", description = "영화 예매 관리 API")
public class ReservationController {

    private final ReservationService reservationService;
    private final ScheduleService scheduleService;
    private final MovieService movieService;
    private final SeatService seatService;
    private final PaymentService paymentService;
    private final MemberService memberService;
    private final BankService bankService;

    // 영화별 상영 가능 날짜 조회
    @GetMapping("/movies/{movieId}/dates")
    @Operation(
        summary = "영화별 상영 가능 날짜 조회",
        description = "특정 영화의 상영 가능한 날짜 목록을 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "상영 가능 날짜 조회 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "movie": {
                            "id": 1,
                            "title": "영화 제목"
                        },
                        "dates": ["2024-01-01", "2024-01-02", "2024-01-03"]
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<Map<String, Object>> getAvailableDates(
            @Parameter(description = "영화 ID", required = true)
            @PathVariable Long movieId) {
        MovieDto movie = movieService.findMovieById(movieId);
        List<String> availableDates = scheduleService.findAvailableDatesForMovie(movieId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("movie", movie);
        response.put("dates", availableDates);
        
        return ResponseEntity.ok(response);
    }

    // 영화 및 날짜별 상영 스케줄 조회
    @GetMapping("/movies/{movieId}/dates/{date}")
    @Operation(
        summary = "영화 및 날짜별 상영 스케줄 조회",
        description = "특정 영화의 특정 날짜 상영 스케줄을 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "상영 스케줄 조회 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "movie": {
                            "id": 1,
                            "title": "영화 제목"
                        },
                        "date": "2024-01-01",
                        "schedules": [
                            {
                                "id": "SCH001",
                                "startTime": "10:00",
                                "endTime": "12:00",
                                "screenId": 1
                            }
                        ]
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<Map<String, Object>> getSchedulesByDate(
            @Parameter(description = "영화 ID", required = true)
            @PathVariable Long movieId,
            @Parameter(description = "상영 날짜 (YYYY-MM-DD)", required = true)
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
    @Operation(
        summary = "스케줄별 좌석 정보 조회",
        description = "특정 상영 스케줄의 좌석 정보와 예약 현황을 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "좌석 정보 조회 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "schedule": {
                            "id": "SCH001",
                            "startTime": "10:00",
                            "movieId": 1
                        },
                        "movie": {
                            "id": 1,
                            "title": "영화 제목"
                        },
                        "seats": [
                            {
                                "id": 1,
                                "row": "A",
                                "number": 1,
                                "type": "STANDARD"
                            }
                        ],
                        "reservedSeatIds": [1, 5, 10]
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<Map<String, Object>> getSeatsForSchedule(
            @Parameter(description = "스케줄 ID", required = true)
            @PathVariable String scheduleId) {
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
    @Operation(
        summary = "예매 정보 확인",
        description = "예매 전 최종 확인을 위한 정보를 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "예매 정보 조회 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "schedule": {
                            "id": "SCH001",
                            "startTime": "10:00",
                            "movieId": 1
                        },
                        "seat": {
                            "id": 1,
                            "row": "A",
                            "number": 1
                        },
                        "movie": {
                            "id": 1,
                            "title": "영화 제목",
                            "price": 12000
                        },
                        "member": {
                            "id": 1,
                            "name": "홍길동",
                            "points": 1500
                        }
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<Map<String, Object>> getReservationInfo(
            @Parameter(description = "스케줄 ID", required = true)
            @RequestParam String scheduleId,
            @Parameter(description = "좌석 ID", required = true)
            @RequestParam Integer seatId) {
        
        // 스케줄 및 좌석 정보 조회
        ScheduleDto schedule = scheduleService.findScheduleById(scheduleId);
        SeatDto seat = seatService.findSeatById(seatId);
        MovieDto movie = movieService.findMovieById(schedule.getMovieId());
        
        // JWT에서 로그인 회원 정보 가져오기
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

    // 예매 생성 (결제 전 단계)
    @PostMapping("/create")
    @Operation(
        summary = "예매 생성",
        description = "영화 예매를 생성합니다. (결제 전 단계)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "예매 생성 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "status": "SUCCESS",
                        "reservationId": "R123456789",
                        "message": "예매가 생성되었습니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "예매 생성 실패",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "실패 응답",
                    value = """
                    {
                        "status": "FAIL",
                        "message": "이미 예약된 좌석입니다."
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<Map<String, Object>> createReservation(
            @Parameter(
                description = "예매 생성 정보",
                required = true,
                content = @Content(
                    examples = @ExampleObject(
                        name = "예매 생성 요청",
                        value = """
                        {
                            "scheduleId": "SCH001",
                            "seatId": 1,
                            "phoneNumber": "010-1234-5678",
                            "discountCode": "STUDENT",
                            "discountAmount": 2000
                        }
                        """
                    )
                )
            )
            @Valid @RequestBody ReservationCreateDto createDto) {
        
        try {
            // JWT에서 회원 정보 추출 (로그인한 경우)
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String userId = null;
            
            if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
                MemberDto member = memberService.findMemberByUserId(auth.getName());
                userId = member.getUserId();
                System.out.println("DEBUG: Found member with ID = " + userId);
            } else {
                System.out.println("DEBUG: No authenticated member found");
            }
            
            // 예매 정보 저장
            String reservationId = reservationService.saveReservation(
                    ReservationSaveDto.builder()
                            .scheduleId(createDto.getScheduleId())
                            .seatId(createDto.getSeatId())
                            .memberUserId(userId)
                            .phoneNumber(createDto.getPhoneNumber())
                            .discountCode(createDto.getDiscountCode())
                            .discountAmount(createDto.getDiscountAmount())
                            .build()
            );
            
            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "reservationId", reservationId,
                    "message", "예매가 생성되었습니다."
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "status", "FAIL",
                    "message", e.getMessage()
            ));
        }
    }

    // 예매 결제 처리
    @PostMapping("/{reservationId}/payment")
    @Operation(
        summary = "예매 결제 처리",
        description = "생성된 예매에 대해 결제를 처리합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "결제 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "status": "SUCCESS",
                        "reservationId": "R123456789",
                        "approvalNumber": "AP123456789",
                        "message": "결제가 완료되었습니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "결제 실패",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "실패 응답",
                    value = """
                    {
                        "status": "FAIL",
                        "message": "결제에 실패했습니다."
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<Map<String, Object>> processPayment(
            @Parameter(description = "예매 ID", required = true)
            @PathVariable String reservationId,
            @Parameter(
                description = "결제 정보",
                required = true,
                content = @Content(
                    examples = @ExampleObject(
                        name = "결제 요청",
                        value = """
                        {
                            "paymentMethod": "card",
                            "amount": 10000,
                            "cardOrAccountNumber": "1234-5678-9012-3456",
                            "deductedPoints": 500
                        }
                        """
                    )
                )
            )
            @Valid @RequestBody PaymentProcessDto paymentDto) {
        
        try {
            // 예매 정보 확인
            ReservationDto reservation = reservationService.findReservationById(reservationId);
            
            // JWT에서 로그인한 회원 정보 가져오기
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            MemberDto member = null;
            
            if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
                member = memberService.findMemberByUserId(auth.getName());
            }
            
            // 권한 확인 (본인 예매만 결제 가능)
            if (reservation.getMemberUserId() != null && !reservation.getMemberUserId().equals(member.getUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                        "status", "FAIL",
                        "message", "결제 권한이 없습니다."
                ));
            }
            
            // 결제 정보 저장
            String paymentId = paymentService.savePayment(
                    PaymentSaveDto.builder()
                            .method(paymentDto.getPaymentMethod())
                            .amount(paymentDto.getAmount())
                            .memberUserId(reservation.getMemberUserId())
                            .deductedPoints(paymentDto.getDeductedPoints())
                            .build()
            );
            
            // 가상의 은행/카드사 통신
            Map<String, Object> paymentResult = bankService.requestPaymentApproval(
                    paymentDto.getPaymentMethod(),
                    paymentDto.getAmount(),
                    paymentDto.getCardOrAccountNumber()
            );
            
            if ("SUCCESS".equals(paymentResult.get("status"))) {
                // 결제 완료 처리
                paymentService.completePayment(paymentId);
                
                // 예매 완료 처리
                reservationService.completeReservation(reservationId, paymentId);
                
                paymentResult.put("reservationId", reservationId);
                return ResponseEntity.ok(paymentResult);
            } else {
                // 결제 실패 시 결제 정보 취소
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
    @Operation(
        summary = "예매 상세 정보 조회",
        description = "예매 ID로 예매 상세 정보를 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "예매 상세 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ReservationDto.class)
            )
        )
    })
    public ResponseEntity<ReservationDto> getReservationDetail(
            @Parameter(description = "예매 ID", required = true)
            @PathVariable String reservationId) {
        ReservationDto reservation = reservationService.findReservationById(reservationId);
        return ResponseEntity.ok(reservation);
    }

    // 회원 예매 내역 조회
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "내 예매 내역 조회",
        description = "로그인한 회원의 예매 내역을 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "예매 내역 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ReservationDto.class)
            )
        )
    })
    public ResponseEntity<List<ReservationDto>> getMyReservations() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        MemberDto member = memberService.findMemberByUserId(auth.getName());
        
        List<ReservationDto> reservations = reservationService.findReservationsByMember(member.getUserId());
        return ResponseEntity.ok(reservations);
    }

    // 비회원 예매 내역 조회
    @GetMapping("/non-member/check")
    @Operation(
        summary = "비회원 예매 내역 조회",
        description = "전화번호와 예매번호로 비회원 예매 내역을 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "비회원 예매 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ReservationDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "예약 정보 불일치",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "정보 불일치",
                    value = """
                    {
                        "error": "예약 정보가 일치하지 않습니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "예약 정보 없음",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "예약 없음",
                    value = """
                    {
                        "error": "존재하지 않는 예약입니다."
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<?> getNonMemberReservation(
            @Parameter(description = "전화번호", required = true)
            @RequestParam String phoneNumber,
            @Parameter(description = "예매 ID", required = true)
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
    @Operation(
        summary = "예매 취소",
        description = "예매를 취소하고 결제를 환불 처리합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "예매 취소 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "status": "SUCCESS",
                        "message": "예매가 취소되었습니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "취소 실패",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "실패 응답",
                    value = """
                    {
                        "status": "FAIL",
                        "message": "취소 처리에 실패했습니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "권한 없음",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "권한 없음",
                    value = """
                    {
                        "status": "FAIL",
                        "message": "취소 권한이 없습니다."
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<Map<String, Object>> cancelReservation(
            @Parameter(description = "예매 ID", required = true)
            @PathVariable String reservationId) {
        try {
            ReservationDto reservation = reservationService.findReservationById(reservationId);
            
            // JWT에서 로그인한 회원 정보 가져오기
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            MemberDto member = null;
            
            if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
                member = memberService.findMemberByUserId(auth.getName());
            }
            
            // 권한 확인 (본인 예매만 취소 가능)
            if (reservation.getMemberUserId() != null && !reservation.getMemberUserId().equals(member.getUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                        "status", "FAIL",
                        "message", "취소 권한이 없습니다."
                ));
            }
            
            // 결제 취소 요청 (은행/카드사 통신)
            if (reservation.getPaymentId() != null) {
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
            } else {
                reservationService.cancelReservation(reservationId);
                return ResponseEntity.ok(Map.of(
                        "status", "SUCCESS",
                        "message", "예매가 취소되었습니다."
                    ));
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
    @Operation(
        summary = "티켓 발급",
        description = "완료된 예매에 대해 티켓을 발급합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "티켓 발급 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "ticketUrl": "https://example.com/tickets/T123456789",
                        "message": "티켓이 발급되었습니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "티켓 발급 실패",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "실패 응답",
                    value = """
                    {
                        "error": "티켓 발급에 실패했습니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "권한 없음",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "권한 없음",
                    value = """
                    {
                        "error": "티켓 발급 권한이 없습니다."
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<Map<String, String>> issueTicket(
            @Parameter(description = "예매 ID", required = true)
            @PathVariable String reservationId) {
        try {
            ReservationDto reservation = reservationService.findReservationById(reservationId);
            
            // JWT에서 로그인한 회원 정보 가져오기
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            MemberDto member = null;
            
            if (auth != null && auth.isAuthenticated() && !auth.getName().equals("anonymousUser")) {
                member = memberService.findMemberByUserId(auth.getName());
            }
            
            // 권한 확인 (본인 예매만 발급 가능)
            if (reservation.getMemberUserId() != null && !reservation.getMemberUserId().equals(member.getUserId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                        "error", "티켓 발급 권한이 없습니다."
                ));
            }
            
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

    // 관리자용: 미결제 예약 현황 조회
    @GetMapping("/admin/unpaid")
    @Operation(
        summary = "미결제 예약 현황 조회 (관리자용)",
        description = "현재 결제가 완료되지 않은 예약 목록을 조회합니다."
    )
    public ResponseEntity<Map<String, Object>> getUnpaidReservations() {
        List<ReservationDto> unpaidReservations = reservationService.findUnpaidReservations();
        
        Map<String, Object> response = new HashMap<>();
        response.put("totalCount", unpaidReservations.size());
        response.put("reservations", unpaidReservations);
        response.put("checkTime", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }
    
    // 관리자용: 수동으로 만료된 예약 취소
    @PostMapping("/admin/cancel-expired")
    @Operation(
        summary = "만료된 예약 수동 취소 (관리자용)",
        description = "지정한 시간(분) 이전의 미결제 예약을 수동으로 취소합니다."
    )
    public ResponseEntity<Map<String, Object>> manualCancelExpiredReservations(
            @Parameter(description = "취소할 예약의 기준 시간 (분)", required = true)
            @RequestParam(defaultValue = "30") int timeoutMinutes) {
        
        List<String> canceledIds = reservationService.cancelExpiredReservations(timeoutMinutes);
        
        Map<String, Object> response = new HashMap<>();
        response.put("canceledCount", canceledIds.size());
        response.put("canceledReservationIds", canceledIds);
        response.put("timeoutMinutes", timeoutMinutes);
        response.put("processTime", LocalDateTime.now());
        
        return ResponseEntity.ok(response);
    }
}