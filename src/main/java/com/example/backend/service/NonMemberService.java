package com.example.backend.service;

import com.example.backend.entity.NonMemberEntity;
import com.example.backend.repository.NonMemberRepository;
import com.example.backend.dto.NonMemberDto;
//import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NonMemberService {
    private final NonMemberRepository nonMemberRepository;

    @Transactional(readOnly = true)
    public List<NonMemberDto> findAllNonMembers() {
        return nonMemberRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private NonMemberDto convertToDto(NonMemberEntity entity) {
        return NonMemberDto.builder()
                .phoneNumber(entity.getPhoneNumber())
                .build();
    }
}