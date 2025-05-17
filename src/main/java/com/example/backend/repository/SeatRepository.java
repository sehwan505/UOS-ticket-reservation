package com.example.backend.repository;

import com.example.backend.entity.ScreenEntity;
import com.example.backend.entity.SeatEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<SeatEntity, Integer> {
    
    List<SeatEntity> findByScreen(ScreenEntity screen);
    
    List<SeatEntity> findByScreenOrderByRowAscColumnAsc(ScreenEntity screen);
}