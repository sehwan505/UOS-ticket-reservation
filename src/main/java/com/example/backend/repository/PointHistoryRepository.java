package com.example.backend.repository;

import com.example.backend.entity.Member;
import com.example.backend.entity.PointHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointHistoryRepository extends JpaRepository<PointHistory, Long> {
    
    Page<PointHistory> findByMember(Member member, Pageable pageable);
}