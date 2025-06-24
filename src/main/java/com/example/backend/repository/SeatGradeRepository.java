package com.example.backend.repository;

import com.example.backend.entity.SeatGradeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatGradeRepository extends JpaRepository<SeatGradeEntity, String> {
}