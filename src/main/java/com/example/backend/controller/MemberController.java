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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final ReservationService reservationService;
    private final PointHistoryService pointHistoryService;

    // 회원가입 폼
    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("memberForm", new MemberSaveDto());
        return "members/signup";
    }

    // 회원가입 처리
    @PostMapping("/signup")
    public String signup(@Validated @ModelAttribute("memberForm") MemberSaveDto memberSaveDto,
                         BindingResult result) {
        
        // 유효성 검사
        if (result.hasErrors()) {
            return "members/signup";
        }
        
        // 아이디 중복 체크
        if (memberService.checkUserIdDuplicate(memberSaveDto.getUserId())) {
            result.rejectValue("userId", "duplicate", "이미 사용중인 아이디입니다.");
            return "members/signup";
        }
        
        // 회원가입 처리
        memberService.saveMember(memberSaveDto);
        
        return "redirect:/login";
    }

    // 로그인 페이지
    @GetMapping("/login")
    public String loginForm() {
        return "members/login";
    }

    // 회원정보 조회
    @GetMapping("/members/my")
    @PreAuthorize("isAuthenticated()")
    public String myProfile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        MemberDto member = memberService.findMemberByUserId(auth.getName());
        
        model.addAttribute("member", member);
        
        return "members/profile";
    }

    // 회원정보 수정 폼
    @GetMapping("/members/my/edit")
    @PreAuthorize("isAuthenticated()")
    public String editProfileForm(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        MemberDto member = memberService.findMemberByUserId(auth.getName());
        
        MemberSaveDto memberForm = MemberSaveDto.builder()
                .userId(member.getUserId())
                .email(member.getEmail())
                .phoneNumber(member.getPhoneNumber())
                .build();
        
        model.addAttribute("memberForm", memberForm);
        
        return "members/edit_profile";
    }

    // 회원정보 수정 처리
    @PostMapping("/members/my")
    @PreAuthorize("isAuthenticated()")
    public String updateProfile(@Validated @ModelAttribute("memberForm") MemberSaveDto memberSaveDto,
                               BindingResult result) {
        
        if (result.hasErrors()) {
            return "members/edit_profile";
        }
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        MemberDto member = memberService.findMemberByUserId(auth.getName());
        
        memberService.updateMember(member.getId(), memberSaveDto);
        
        return "redirect:/members/my";
    }

    // 포인트 내역 조회
    @GetMapping("/members/my/points")
    @PreAuthorize("isAuthenticated()")
    public String myPoints(
            @PageableDefault(size = 10) Pageable pageable,
            Model model) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        MemberDto member = memberService.findMemberByUserId(auth.getName());
        
        Page<PointHistoryDto> pointHistory = pointHistoryService.findPointHistoryByMember(member.getId(), pageable);
        
        model.addAttribute("member", member);
        model.addAttribute("pointHistory", pointHistory);
        
        return "members/points";
    }

    // 아이디 중복 체크 API
    @GetMapping("/api/members/check-id")
    @ResponseBody
    public Map<String, Boolean> checkUserIdDuplicate(@RequestParam String userId) {
        boolean isDuplicate = memberService.checkUserIdDuplicate(userId);
        return Map.of("duplicate", isDuplicate);
    }

    // 관리자용 회원 목록 조회
    @GetMapping("/admin/members")
    @PreAuthorize("hasRole('ADMIN')")
    public String memberList(Model model) {
        List<MemberDto> members = memberService.findAllMembers();
        model.addAttribute("members", members);
        return "admin/members/list";
    }

    // 관리자용 회원 상세 조회
    @GetMapping("/admin/members/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String memberDetail(@PathVariable Long id, Model model) {
        MemberDto member = memberService.findMemberById(id);
        List<ReservationDto> reservations = reservationService.findReservationsByMember(id);
        
        model.addAttribute("member", member);
        model.addAttribute("reservations", reservations);
        
        return "admin/members/detail";
    }
}