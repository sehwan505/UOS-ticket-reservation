package com.example.backend.repository;

import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.PointHistoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointHistoryRepository extends JpaRepository<PointHistoryEntity, Long> {
    
    Page<PointHistoryEntity> findByMember(MemberEntity member, Pageable pageable);
}