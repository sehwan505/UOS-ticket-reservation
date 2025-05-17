package com.example.backend.service;

import com.example.backend.dto.MemberDto;
import com.example.backend.dto.MemberSaveDto;
import com.example.backend.entity.Member;
import com.example.backend.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService implements UserDetailsService {
    
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    
    // 회원 상세 조회 (Spring Security용)
    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        return memberRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + userId));
    }
    
    // 모든 회원 조회
    public List<MemberDto> findAllMembers() {
        return memberRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    // 회원 상세 조회
    public MemberDto findMemberById(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. ID: " + id));
        return convertToDto(member);
    }
    
    // 회원 등록
    @Transactional
    public Long saveMember(MemberSaveDto memberSaveDto) {
        // 아이디 중복 체크
        if (memberRepository.existsByUserId(memberSaveDto.getUserId())) {
            throw new IllegalArgumentException("이미 사용 중인 아이디입니다.");
        }
        
        // 이메일 중복 체크
        if (memberRepository.existsByEmail(memberSaveDto.getEmail())) {
            throw new IllegalArgumentException("이미 사용 중인 이메일입니다.");
        }
        
        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(memberSaveDto.getPassword());
        
        Member member = Member.builder()
                .userId(memberSaveDto.getUserId())
                .password(encodedPassword)
                .email(memberSaveDto.getEmail())
                .phoneNumber(memberSaveDto.getPhoneNumber())
                .birthDate(memberSaveDto.getBirthDate())
                .grade("1") // 기본 등급
                .availablePoints(0) // 초기 포인트 0
                .build();
        
        Member savedMember = memberRepository.save(member);
        return savedMember.getId();
    }
    
    // 회원 수정
    @Transactional
    public Long updateMember(Long id, MemberSaveDto memberSaveDto) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. ID: " + id));
        
        // 기본 정보 업데이트
        member.setEmail(memberSaveDto.getEmail());
        member.setPhoneNumber(memberSaveDto.getPhoneNumber());
        
        // 비밀번호 변경 (새 비밀번호가 제공된 경우)
        if (memberSaveDto.getPassword() != null && !memberSaveDto.getPassword().isEmpty()) {
            member.setPassword(passwordEncoder.encode(memberSaveDto.getPassword()));
        }
        
        return member.getId();
    }
    
    // 회원 삭제
    @Transactional
    public void deleteMember(Long id) {
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. ID: " + id));
        
        memberRepository.delete(member);
    }
    
    // 포인트 추가
    @Transactional
    public Integer addPoints(Long memberId, int points) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. ID: " + memberId));
        
        member.setAvailablePoints(member.getAvailablePoints() + points);
        
        return member.getAvailablePoints();
    }
    
    // 포인트 사용
    @Transactional
    public Integer usePoints(Long memberId, int points) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. ID: " + memberId));
        
        if (member.getAvailablePoints() < points) {
            throw new IllegalArgumentException("사용 가능한 포인트가 부족합니다.");
        }
        
        member.setAvailablePoints(member.getAvailablePoints() - points);
        
        return member.getAvailablePoints();
    }
    
    // ID 중복 체크
    public boolean checkUserIdDuplicate(String userId) {
        return memberRepository.existsByUserId(userId);
    }
    
    // 전화번호로 회원 찾기
    public MemberDto findMemberByPhoneNumber(String phoneNumber) {
        return memberRepository.findByPhoneNumber(phoneNumber)
                .map(this::convertToDto)
                .orElse(null);
    }
    
    // Entity를 DTO로 변환
    private MemberDto convertToDto(Member member) {
        return MemberDto.builder()
                .id(member.getId())
                .userId(member.getUserId())
                .email(member.getEmail())
                .phoneNumber(member.getPhoneNumber())
                .birthDate(member.getBirthDate())
                .grade(member.getGrade())
                .availablePoints(member.getAvailablePoints())
                .build();
    }
}