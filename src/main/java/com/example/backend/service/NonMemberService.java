package com.example.backend.service;

import com.example.backend.domain.NonMember;
import com.example.backend.repository.NonMemberRepository;
//import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NonMemberService {
    private final NonMemberRepository nonMemberRepository;

    @Transactional(readOnly = true)
    public List<NonMember> findAllNonMembers() {
        return nonMemberRepository.findAll();
    }
}