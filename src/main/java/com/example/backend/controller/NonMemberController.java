package com.example.backend.controller;

import com.example.backend.entity.NonMemberEntity;
import com.example.backend.service.NonMemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class NonMemberController {

    private final NonMemberService nonMemberService;

    @GetMapping("/nonmembers")
    public ResponseEntity<List<NonMemberEntity>> getAllNonMembers() {
        List<NonMemberEntity> allNonMembers = nonMemberService.findAllNonMembers();
        return ResponseEntity.ok(allNonMembers);
    }
}