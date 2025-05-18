package com.example.backend.controller;

import com.example.backend.dto.MemberDto;
import com.example.backend.dto.MemberSaveDto;
import com.example.backend.dto.PointHistoryDto;
import com.example.backend.dto.ReservationDto;
import com.example.backend.service.MemberService;
import com.example.backend.service.PointHistoryService;
import com.example.backend.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
@RequestMapping("/api")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final ReservationService reservationService;
    private final PointHistoryService pointHistoryService;

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody MemberSaveDto memberSaveDto) {
        // 아이디 중복 체크
        if (memberService.checkUserIdDuplicate(memberSaveDto.getUserId())) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "duplicate",
                    "message", "이미 사용중인 아이디입니다."
            ));
        }
        
        // 회원가입 처리
        Long memberId = memberService.saveMember(memberSaveDto);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "id", memberId,
                "message", "회원가입이 완료되었습니다."
        ));
    }

    // 회원정보 조회
    @GetMapping("/members/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<MemberDto> getMyProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        MemberDto member = memberService.findMemberByUserId(auth.getName());
        
        return ResponseEntity.ok(member);
    }

    // 회원정보 수정
    @PutMapping("/members/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateProfile(@Valid @RequestBody MemberSaveDto memberSaveDto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        MemberDto member = memberService.findMemberByUserId(auth.getName());
        
        memberService.updateMember(member.getId(), memberSaveDto);
        
        return ResponseEntity.ok(Map.of("message", "회원정보가 수정되었습니다."));
    }

    // 포인트 내역 조회
    @GetMapping("/members/my/points")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getMyPoints(
            @PageableDefault(size = 10) Pageable pageable) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        MemberDto member = memberService.findMemberByUserId(auth.getName());
        
        Page<PointHistoryDto> pointHistory = pointHistoryService.findPointHistoryByMember(member.getId(), pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("member", member);
        response.put("pointHistory", pointHistory);
        
        return ResponseEntity.ok(response);
    }

    // 아이디 중복 체크
    @GetMapping("/members/check-id")
    public ResponseEntity<Map<String, Boolean>> checkUserIdDuplicate(@RequestParam String userId) {
        boolean isDuplicate = memberService.checkUserIdDuplicate(userId);
        return ResponseEntity.ok(Map.of("duplicate", isDuplicate));
    }

    // 관리자용 회원 목록 조회
    @GetMapping("/admin/members")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<MemberDto>> getMemberList() {
        List<MemberDto> members = memberService.findAllMembers();
        return ResponseEntity.ok(members);
    }

    // 관리자용 회원 상세 조회
    @GetMapping("/admin/members/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getMemberDetail(@PathVariable Long id) {
        MemberDto member = memberService.findMemberById(id);
        List<ReservationDto> reservations = reservationService.findReservationsByMember(id);
        
        Map<String, Object> response = new HashMap<>();
        response.put("member", member);
        response.put("reservations", reservations);
        
        return ResponseEntity.ok(response);
    }
}