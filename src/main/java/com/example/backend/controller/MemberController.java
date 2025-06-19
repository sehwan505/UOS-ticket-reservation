package com.example.backend.controller;

import com.example.backend.dto.LoginRequest;
import com.example.backend.dto.LoginResponse;
import com.example.backend.dto.MemberDto;
import com.example.backend.dto.MemberSaveDto;
import com.example.backend.dto.PointHistoryDto;
import com.example.backend.dto.ReservationDto;
import com.example.backend.service.MemberService;
import com.example.backend.service.PointHistoryService;
import com.example.backend.service.ReservationService;
import com.example.backend.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Member API", description = "회원 관리 API")
public class MemberController {

    private final MemberService memberService;
    private final ReservationService reservationService;
    private final PointHistoryService pointHistoryService;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    // 로그인
    @PostMapping("/login")
    @Operation(
        summary = "로그인",
        description = "사용자 인증 후 JWT 토큰을 발급합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "로그인 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = LoginResponse.class),
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                        "tokenType": "Bearer",
                        "expiresIn": 86400,
                        "member": {
                            "id": 1,
                            "userId": "testuser",
                            "email": "test@example.com",
                            "phoneNumber": "01012345678",
                            "grade": "1",
                            "availablePoints": 1000
                        }
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "로그인 실패",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "실패 응답",
                    value = """
                    {
                        "error": "authentication_failed",
                        "message": "아이디 또는 비밀번호가 올바르지 않습니다."
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<?> login(
            @Parameter(
                description = "로그인 정보",
                required = true,
                content = @Content(
                    examples = @ExampleObject(
                        name = "로그인 요청",
                        value = """
                        {
                            "userId": "testuser",
                            "password": "password123"
                        }
                        """
                    )
                )
            )
            @Valid @RequestBody LoginRequest loginRequest) {
        
        try {
            // 사용자 인증
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getUserId(),
                    loginRequest.getPassword()
                )
            );

            // 인증된 사용자 정보 가져오기
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            MemberDto member = memberService.findMemberByUserId(userDetails.getUsername());

            // JWT 토큰 생성
            String accessToken = jwtUtil.generateToken(userDetails);

            // 응답 생성
            LoginResponse response = LoginResponse.builder()
                    .accessToken(accessToken)
                    .tokenType("Bearer")
                    .expiresIn(86400L) // 24시간
                    .member(member)
                    .build();

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(401).body(Map.of(
                    "error", "authentication_failed",
                    "message", "아이디 또는 비밀번호가 올바르지 않습니다."
            ));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of(
                    "error", "internal_server_error",
                    "message", "로그인 처리 중 오류가 발생했습니다."
            ));
        }
    }

    // 로그아웃
    @PostMapping("/logout")
    @Operation(
        summary = "로그아웃",
        description = "현재 세션을 종료합니다. (클라이언트에서 토큰 삭제 필요)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "로그아웃 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "message": "로그아웃되었습니다."
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<Map<String, String>> logout() {
        // JWT는 stateless이므로 서버에서 할 일은 없음
        // 클라이언트에서 토큰을 삭제해야 함
        return ResponseEntity.ok(Map.of("message", "로그아웃되었습니다."));
    }

    // 회원가입
    @PostMapping("/signup")
    @Operation(
        summary = "회원가입",
        description = "새로운 회원을 등록합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "회원가입 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "id": 1,
                        "message": "회원가입이 완료되었습니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "아이디 중복",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "중복 오류",
                    value = """
                    {
                        "error": "duplicate",
                        "message": "이미 사용중인 아이디입니다."
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<?> signup(
            @Parameter(
                description = "회원가입 정보",
                required = true,
                content = @Content(
                    examples = @ExampleObject(
                        name = "회원가입 요청",
                        value = """
                        {
                            "userId": "testuser",
                            "password": "password123",
                            "name": "홍길동",
                            "email": "test@example.com",
                            "phoneNumber": "010-1234-5678"
                        }
                        """
                    )
                )
            )
            @Valid @RequestBody MemberSaveDto memberSaveDto) {
        // 아이디 중복 체크
        if (memberService.checkUserIdDuplicate(memberSaveDto.getUserId())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "duplicate",
                    "message", "이미 사용중인 아이디입니다."
            ));
        }
        
        // 회원가입 처리
        String userId = memberService.saveMember(memberSaveDto);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", userId,
                "message", "회원가입이 완료되었습니다."
        ));
    }

    // 회원정보 조회
    @GetMapping("/members/my")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "내 정보 조회",
        description = "로그인한 사용자의 정보를 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "회원정보 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = MemberDto.class)
            )
        )
    })
    public ResponseEntity<MemberDto> getMyProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        MemberDto member = memberService.findMemberByUserId(auth.getName());
        
        return ResponseEntity.ok(member);
    }

    // 회원정보 수정
    @PutMapping("/members/my")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "내 정보 수정",
        description = "로그인한 사용자의 정보를 수정합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "회원정보 수정 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "message": "회원정보가 수정되었습니다."
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<?> updateProfile(
            @Parameter(
                description = "수정할 회원 정보",
                required = true,
                content = @Content(
                    examples = @ExampleObject(
                        name = "회원정보 수정 요청",
                        value = """
                        {
                            "name": "홍길동",
                            "email": "newemail@example.com",
                            "phoneNumber": "010-9876-5432"
                        }
                        """
                    )
                )
            )
            @Valid @RequestBody MemberSaveDto memberSaveDto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        MemberDto member = memberService.findMemberByUserId(auth.getName());
        
        memberService.updateMember(member.getUserId(), memberSaveDto);
        
        return ResponseEntity.ok(Map.of("message", "회원정보가 수정되었습니다."));
    }

    // 포인트 내역 조회
    @GetMapping("/members/my/points")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "내 포인트 내역 조회",
        description = "로그인한 사용자의 포인트 적립/사용 내역을 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "포인트 내역 조회 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "member": {
                            "id": 1,
                            "name": "홍길동",
                            "points": 1500
                        },
                        "pointHistory": {
                            "content": [
                                {
                                    "id": 1,
                                    "type": "EARN",
                                    "amount": 500,
                                    "description": "영화 예매 적립",
                                    "createdAt": "2024-01-01T10:00:00"
                                }
                            ],
                            "totalElements": 10
                        }
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<Map<String, Object>> getMyPoints(
            @Parameter(description = "페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10") 
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        MemberDto member = memberService.findMemberByUserId(auth.getName());
        
        Page<PointHistoryDto> pointHistory = pointHistoryService.findPointHistoryByMember(member.getUserId(), pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("member", member);
        response.put("pointHistory", pointHistory);
        
        return ResponseEntity.ok(response);
    }

    // 아이디 중복 체크
    @GetMapping("/members/check-id")
    @Operation(
        summary = "아이디 중복 체크",
        description = "회원가입 시 아이디 중복 여부를 확인합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "중복 체크 완료",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "중복 체크 응답",
                    value = """
                    {
                        "duplicate": false
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<Map<String, Boolean>> checkUserIdDuplicate(
            @Parameter(description = "확인할 사용자 ID", required = true)
            @RequestParam String userId) {
        boolean isDuplicate = memberService.checkUserIdDuplicate(userId);
        return ResponseEntity.ok(Map.of("duplicate", isDuplicate));
    }

    // 관리자용 회원 목록 조회
    @GetMapping("/admin/members")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "전체 회원 목록 조회",
        description = "관리자가 전체 회원 목록을 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "회원 목록 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = MemberDto.class)
            )
        )
    })
    public ResponseEntity<List<MemberDto>> getMemberList() {
        List<MemberDto> members = memberService.findAllMembers();
        return ResponseEntity.ok(members);
    }

    // 관리자용 회원 상세 조회
    @GetMapping("/admin/members/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "회원 상세 정보 조회",
        description = "관리자가 특정 회원의 상세 정보와 예매 내역을 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "회원 상세 조회 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "member": {
                            "id": 1,
                            "userId": "testuser",
                            "name": "홍길동",
                            "email": "test@example.com",
                            "points": 1500
                        },
                        "reservations": [
                            {
                                "id": "R123456",
                                "movieTitle": "영화 제목",
                                "scheduleDate": "2024-01-01",
                                "status": "COMPLETED"
                            }
                        ]
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<Map<String, Object>> getMemberDetail(
            @Parameter(description = "회원 ID", required = true)
            @PathVariable String userId) {
        
        MemberDto member = memberService.findMemberById(userId);
        List<ReservationDto> reservations = reservationService.findReservationsByMember(userId);
        
        Map<String, Object> response = Map.of(
                "member", member,
                "reservations", reservations
        );
        
        return ResponseEntity.ok(response);
    }
}