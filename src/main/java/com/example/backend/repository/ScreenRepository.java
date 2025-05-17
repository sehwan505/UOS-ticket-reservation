package com.example.backend.repository;

import com.example.backend.entity.Cinema;
import com.example.backend.entity.Screen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScreenRepository extends JpaRepository<Screen, String> {
    
    List<Screen> findByCinema(Cinema cinema);
}