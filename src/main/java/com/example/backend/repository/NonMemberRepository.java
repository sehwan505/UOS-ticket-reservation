package com.example.backend.repository;

import com.example.backend.entity.NonMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NonMemberRepository extends JpaRepository<NonMemberEntity, String> {
}