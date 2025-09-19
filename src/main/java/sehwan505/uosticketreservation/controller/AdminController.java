package sehwan505.uosticketreservation.controller;

import sehwan505.uosticketreservation.dto.*;
import sehwan505.uosticketreservation.service.CinemaService;
import sehwan505.uosticketreservation.service.ScheduleService;
import sehwan505.uosticketreservation.service.ScreenService;
import sehwan505.uosticketreservation.service.SeatService;
import sehwan505.uosticketreservation.service.MemberService;
import sehwan505.uosticketreservation.service.NonMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "관리자 기능 API")
public class AdminController {
    
    private final CinemaService cinemaService;
    private final ScreenService screenService;
    private final SeatService seatService;
    private final ScheduleService scheduleService;
    private final MemberService memberService;
    private final NonMemberService nonMemberService;
    
    // ===== 영화관 관리 =====
    
    @PostMapping("/cinemas")
    @Operation(summary = "영화관 등록", description = "새로운 영화관을 등록합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "영화관 등록 성공"),
        @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    public ResponseEntity<Map<String, Object>> createCinema(@RequestBody CinemaSaveDto cinemaSaveDto) {
        try {
            String cinemaId = cinemaService.saveCinema(cinemaSaveDto);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "영화관이 성공적으로 등록되었습니다.");
            response.put("cinemaId", cinemaId);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PutMapping("/cinemas/{cinemaId}")
    @Operation(summary = "영화관 정보 수정", description = "기존 영화관 정보를 수정합니다.")
    public ResponseEntity<Map<String, Object>> updateCinema(
            @Parameter(description = "영화관 ID", required = true) @PathVariable String cinemaId,
            @RequestBody CinemaSaveDto cinemaSaveDto) {
        try {
            String updatedId = cinemaService.updateCinema(cinemaId, cinemaSaveDto);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "영화관 정보가 성공적으로 수정되었습니다.");
            response.put("cinemaId", updatedId);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @DeleteMapping("/cinemas/{cinemaId}")
    @Operation(summary = "영화관 삭제", description = "영화관을 삭제합니다.")
    public ResponseEntity<Map<String, Object>> deleteCinema(
            @Parameter(description = "영화관 ID", required = true) @PathVariable String cinemaId) {
        try {
            cinemaService.deleteCinema(cinemaId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "영화관이 성공적으로 삭제되었습니다.");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // ===== 상영관 관리 =====
    
    @GetMapping("/screens")
    @Operation(summary = "모든 상영관 조회", description = "모든 상영관 목록을 조회합니다.")
    public ResponseEntity<List<ScreenDto>> getAllScreens() {
        List<ScreenDto> screens = screenService.findAllScreens();
        return ResponseEntity.ok(screens);
    }
    
    @GetMapping("/screens/{screenId}")
    @Operation(summary = "상영관 상세 조회", description = "특정 상영관의 상세 정보를 조회합니다.")
    public ResponseEntity<ScreenDto> getScreen(
            @Parameter(description = "상영관 ID", required = true) @PathVariable String screenId) {
        ScreenDto screen = screenService.findScreenById(screenId);
        return ResponseEntity.ok(screen);
    }
    
    @GetMapping("/cinemas/{cinemaId}/screens")
    @Operation(summary = "영화관별 상영관 조회", description = "특정 영화관의 상영관 목록을 조회합니다.")
    public ResponseEntity<List<ScreenDto>> getScreensByCinema(
            @Parameter(description = "영화관 ID", required = true) @PathVariable String cinemaId) {
        List<ScreenDto> screens = screenService.findScreensByCinema(cinemaId);
        return ResponseEntity.ok(screens);
    }
    
    @PostMapping("/screens")
    @Operation(summary = "상영관 등록", description = "새로운 상영관을 등록합니다.")
    public ResponseEntity<Map<String, Object>> createScreen(@RequestBody ScreenSaveDto screenSaveDto) {
        try {
            String screenId = screenService.saveScreen(screenSaveDto);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "상영관이 성공적으로 등록되었습니다.");
            response.put("screenId", screenId);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PutMapping("/screens/{screenId}")
    @Operation(summary = "상영관 정보 수정", description = "기존 상영관 정보를 수정합니다.")
    public ResponseEntity<Map<String, Object>> updateScreen(
            @Parameter(description = "상영관 ID", required = true) @PathVariable String screenId,
            @RequestBody ScreenSaveDto screenSaveDto) {
        try {
            String updatedId = screenService.updateScreen(screenId, screenSaveDto);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "상영관 정보가 성공적으로 수정되었습니다.");
            response.put("screenId", updatedId);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @DeleteMapping("/screens/{screenId}")
    @Operation(summary = "상영관 삭제", description = "상영관을 삭제합니다.")
    public ResponseEntity<Map<String, Object>> deleteScreen(
            @Parameter(description = "상영관 ID", required = true) @PathVariable String screenId) {
        try {
            screenService.deleteScreen(screenId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "상영관이 성공적으로 삭제되었습니다.");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // ===== 좌석 관리 =====
    
    @GetMapping("/seats")
    @Operation(summary = "모든 좌석 조회", description = "모든 좌석 목록을 조회합니다.")
    public ResponseEntity<List<SeatDto>> getAllSeats() {
        List<SeatDto> seats = seatService.findAllSeats();
        return ResponseEntity.ok(seats);
    }
    
    @GetMapping("/seats/{seatId}")
    @Operation(summary = "좌석 상세 조회", description = "특정 좌석의 상세 정보를 조회합니다.")
    public ResponseEntity<SeatDto> getSeat(
            @Parameter(description = "좌석 ID", required = true) @PathVariable Integer seatId) {
        SeatDto seat = seatService.findSeatById(seatId);
        return ResponseEntity.ok(seat);
    }
    
    @GetMapping("/screens/{screenId}/seats")
    @Operation(summary = "상영관별 좌석 조회", description = "특정 상영관의 좌석 목록을 조회합니다.")
    public ResponseEntity<List<SeatDto>> getSeatsByScreen(
            @Parameter(description = "상영관 ID", required = true) @PathVariable String screenId) {
        List<SeatDto> seats = seatService.findSeatsByScreen(screenId);
        return ResponseEntity.ok(seats);
    }
    
    @PostMapping("/seats")
    @Operation(summary = "좌석 등록", description = "새로운 좌석을 등록합니다.")
    public ResponseEntity<Map<String, Object>> createSeat(@RequestBody SeatSaveDto seatSaveDto) {
        try {
            Integer seatId = seatService.saveSeat(seatSaveDto);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "좌석이 성공적으로 등록되었습니다.");
            response.put("seatId", seatId);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PutMapping("/seats/{seatId}")
    @Operation(summary = "좌석 정보 수정", description = "기존 좌석 정보를 수정합니다.")
    public ResponseEntity<Map<String, Object>> updateSeat(
            @Parameter(description = "좌석 ID", required = true) @PathVariable Integer seatId,
            @RequestBody SeatSaveDto seatSaveDto) {
        try {
            Integer updatedId = seatService.updateSeat(seatId, seatSaveDto);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "좌석 정보가 성공적으로 수정되었습니다.");
            response.put("seatId", updatedId);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @DeleteMapping("/seats/{seatId}")
    @Operation(summary = "좌석 삭제", description = "좌석을 삭제합니다.")
    public ResponseEntity<Map<String, Object>> deleteSeat(
            @Parameter(description = "좌석 ID", required = true) @PathVariable Integer seatId) {
        try {
            seatService.deleteSeat(seatId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "좌석이 성공적으로 삭제되었습니다.");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/screens/{screenId}/seats/bulk")
    @Operation(summary = "상영관 좌석 일괄 생성", description = "상영관에 격자 형태로 좌석을 일괄 생성합니다.")
    public ResponseEntity<Map<String, Object>> createSeatsForScreen(
            @Parameter(description = "상영관 ID", required = true) @PathVariable String screenId,
            @RequestParam String seatGradeId,
            @RequestParam int rows,
            @RequestParam int seatsPerRow) {
        try {
            List<Integer> createdSeatIds = seatService.createSeatsForScreen(screenId, seatGradeId, rows, seatsPerRow);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "좌석이 일괄 생성되었습니다.");
            response.put("createdSeats", createdSeatIds.size());
            response.put("seatIds", createdSeatIds);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // ===== 상영 일정 관리 =====
    
    @GetMapping("/schedules")
    @Operation(
        summary = "모든 상영 일정 조회", 
        description = "모든 상영 일정 목록을 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "상영 일정 조회 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    [
                        {
                            "id": "2501011001",
                            "movieId": 1,
                            "movieTitle": "아바타: 물의 길",
                            "screenId": "01",
                            "screenName": "1관",
                            "cinemaName": "강남점",
                            "screeningDate": "20250101",
                            "screeningStartTime": "2025-01-01T10:00:00",
                            "runtime": 192
                        }
                    ]
                    """
                )
            )
        )
    })
    public ResponseEntity<List<ScheduleDto>> getAllSchedules() {
        List<ScheduleDto> schedules = scheduleService.findAllSchedules();
        return ResponseEntity.ok(schedules);
    }
    
    @GetMapping("/schedules/{scheduleId}")
    @Operation(
        summary = "상영 일정 상세 조회", 
        description = "특정 상영 일정의 상세 정보를 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "상영 일정 상세 조회 성공"
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "상영 일정을 찾을 수 없음"
        )
    })
    public ResponseEntity<ScheduleDto> getSchedule(
            @Parameter(description = "상영 일정 ID", required = true) 
            @PathVariable String scheduleId) {
        ScheduleDto schedule = scheduleService.findScheduleById(scheduleId);
        return ResponseEntity.ok(schedule);
    }
    
    @PostMapping("/schedules")
    @Operation(
        summary = "신규 상영 일정 등록", 
        description = "새로운 상영 일정을 등록합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201", 
            description = "상영 일정 등록 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "success": true,
                        "message": "상영 일정이 성공적으로 등록되었습니다.",
                        "scheduleId": "2501011001"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "잘못된 요청",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "실패 응답",
                    value = """
                    {
                        "success": false,
                        "message": "존재하지 않는 영화입니다. ID: 999"
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<Map<String, Object>> createSchedule(
            @Parameter(
                description = "상영 일정 정보",
                required = true,
                content = @Content(
                    examples = @ExampleObject(
                        name = "상영 일정 등록 요청",
                        value = """
                        {
                            "movieId": 1,
                            "screenId": "01",
                            "screeningDate": "20250101",
                            "screeningStartTime": "1000"
                        }
                        """
                    )
                )
            )
            @Valid @RequestBody ScheduleSaveDto scheduleSaveDto) {
        try {
            String scheduleId = scheduleService.saveSchedule(scheduleSaveDto);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "상영 일정이 성공적으로 등록되었습니다.");
            response.put("scheduleId", scheduleId);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PutMapping("/schedules/{scheduleId}")
    @Operation(
        summary = "상영 일정 수정", 
        description = "기존 상영 일정 정보를 수정합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "상영 일정 수정 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "success": true,
                        "message": "상영 일정이 성공적으로 수정되었습니다.",
                        "scheduleId": "2501011001"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "잘못된 요청"
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "상영 일정을 찾을 수 없음"
        )
    })
    public ResponseEntity<Map<String, Object>> updateSchedule(
            @Parameter(description = "상영 일정 ID", required = true) 
            @PathVariable String scheduleId,
            @Parameter(
                description = "수정할 상영 일정 정보",
                required = true,
                content = @Content(
                    examples = @ExampleObject(
                        name = "상영 일정 수정 요청",
                        value = """
                        {
                            "movieId": 2,
                            "screenId": "02",
                            "screeningDate": "20250101",
                            "screeningStartTime": "1400"
                        }
                        """
                    )
                )
            )
            @Valid @RequestBody ScheduleSaveDto scheduleSaveDto) {
        try {
            String updatedScheduleId = scheduleService.updateSchedule(scheduleId, scheduleSaveDto);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "상영 일정이 성공적으로 수정되었습니다.");
            response.put("scheduleId", updatedScheduleId);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @DeleteMapping("/schedules/{scheduleId}")
    @Operation(
        summary = "상영 일정 삭제", 
        description = "상영 일정을 삭제합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "상영 일정 삭제 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "success": true,
                        "message": "상영 일정이 성공적으로 삭제되었습니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "삭제 실패",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "실패 응답",
                    value = """
                    {
                        "success": false,
                        "message": "예약이 있는 상영 일정은 삭제할 수 없습니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "상영 일정을 찾을 수 없음"
        )
    })
    public ResponseEntity<Map<String, Object>> deleteSchedule(
            @Parameter(description = "상영 일정 ID", required = true) 
            @PathVariable String scheduleId) {
        try {
            scheduleService.deleteSchedule(scheduleId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "상영 일정이 성공적으로 삭제되었습니다.");
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // ===== 멤버 관리 =====
    
    @GetMapping("/members")
    @Operation(
        summary = "모든 회원 조회", 
        description = "모든 회원 목록을 조회합니다. 각 회원의 ID(userId)가 포함되어 있어 상세 조회 API 호출 시 사용할 수 있습니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "회원 목록 조회 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "회원 목록 응답",
                    value = """
                    [
                        {
                            "userId": "user001",
                            "email": "user001@example.com",
                            "phoneNumber": "010-1234-5678",
                            "birthDate": "19901225",
                            "grade": "G",
                            "availablePoints": 1500,
                            "gradeText": "골드"
                        },
                        {
                            "userId": "user002", 
                            "email": "user002@example.com",
                            "phoneNumber": "010-9876-5432",
                            "birthDate": "19851010",
                            "grade": "S",
                            "availablePoints": 500,
                            "gradeText": "실버"
                        }
                    ]
                    """
                )
            )
        )
    })
    public ResponseEntity<List<MemberDto>> getAllMembers() {
        List<MemberDto> members = memberService.findAllMembers();
        return ResponseEntity.ok(members);
    }
    
    @GetMapping("/members/{userId}")
    @Operation(
        summary = "회원 상세 조회", 
        description = "특정 회원의 상세 정보를 조회합니다. 회원 목록 조회에서 받은 userId를 사용하여 호출합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "회원 상세 조회 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "회원 상세 정보",
                    value = """
                    {
                        "userId": "user001",
                        "email": "user001@example.com", 
                        "phoneNumber": "010-1234-5678",
                        "birthDate": "19901225",
                        "grade": "G",
                        "availablePoints": 1500,
                        "gradeText": "골드"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "회원을 찾을 수 없음",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "회원 없음 오류",
                    value = """
                    {
                        "success": false,
                        "message": "존재하지 않는 회원입니다. ID: user999"
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<MemberDto> getMember(
            @Parameter(
                description = "회원 ID (회원 목록 조회에서 받은 userId 사용)", 
                required = true,
                example = "user001"
            ) 
            @PathVariable String userId) {
        MemberDto member = memberService.findMemberById(userId);
        return ResponseEntity.ok(member);
    }
    
    // ===== 비회원 관리 =====
    
    @GetMapping("/nonmembers")
    @Operation(
        summary = "모든 비회원 조회", 
        description = "모든 비회원 목록과 각각의 예약 내역을 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "비회원 목록 조회 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "비회원 목록 응답",
                    value = """
                    [
                        {
                            "phoneNumber": "010-1111-2222",
                            "totalReservations": 3,
                            "completedReservations": 2,
                            "cancelledReservations": 1,
                            "reservations": [
                                {
                                    "id": "250101100101",
                                    "movieTitle": "아바타: 물의 길",
                                    "screenName": "1관",
                                    "cinemaName": "강남점",
                                    "status": "Y",
                                    "reservationTime": "2025-01-01T09:30:00",
                                    "finalPrice": 12000
                                }
                            ]
                        }
                    ]
                    """
                )
            )
        )
    })
    public ResponseEntity<List<NonMemberWithReservationsDto>> getAllNonMembersWithReservations() {
        List<NonMemberWithReservationsDto> nonMembers = nonMemberService.findAllNonMembersWithReservations();
        return ResponseEntity.ok(nonMembers);
    }
    
    @GetMapping("/nonmembers/{phoneNumber}")
    @Operation(
        summary = "비회원 상세 조회", 
        description = "특정 비회원의 상세 정보와 예약 내역을 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "비회원 상세 조회 성공"
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "비회원을 찾을 수 없음"
        )
    })
    public ResponseEntity<NonMemberWithReservationsDto> getNonMemberWithReservations(
            @Parameter(
                description = "비회원 전화번호", 
                required = true,
                example = "010-1111-2222"
            ) 
            @PathVariable String phoneNumber) {
        NonMemberWithReservationsDto nonMember = nonMemberService.findNonMemberWithReservations(phoneNumber);
        return ResponseEntity.ok(nonMember);
    }
} 