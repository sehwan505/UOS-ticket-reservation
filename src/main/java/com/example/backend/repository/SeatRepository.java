package com.example.backend.repository;

import com.example.backend.entity.Screen;
import com.example.backend.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Integer> {
    
    List<Seat> findByScreen(Screen screen);
    
    List<Seat> findByScreenOrderByRowAscColumnAsc(Screen screen);
}