package com.example.backend.repository;

import com.example.backend.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<MemberEntity, String> {
    
    boolean existsByUserId(String userId);
    
    boolean existsByEmail(String email);
    
    Optional<MemberEntity> findByPhoneNumber(String phoneNumber);
}