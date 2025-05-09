package com.example.backend.controller;

import com.example.backend.domain.NonMember;
import com.example.backend.service.NonMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class NonMemberController {

    private final NonMemberService nonMemberService;

    @GetMapping("/nonmember")
    public String showMemberInfo(Model model) {
        List<NonMember> allNonMembers = nonMemberService.findAllNonMembers();
        model.addAttribute("nonMembers", allNonMembers);
        return "home";
    }
}