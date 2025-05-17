package com.example.backend.repository;

import com.example.backend.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    
    Optional<Member> findByUserId(String userId);
    
    boolean existsByUserId(String userId);
    
    boolean existsByEmail(String email);
    
    Optional<Member> findByPhoneNumber(String phoneNumber);
}