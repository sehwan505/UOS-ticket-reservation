package com.example.backend.repository;

import com.example.backend.entity.CinemaEntity;
import com.example.backend.entity.ScreenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScreenRepository extends JpaRepository<ScreenEntity, String> {
    
    List<ScreenEntity> findByCinema(CinemaEntity cinema);
}