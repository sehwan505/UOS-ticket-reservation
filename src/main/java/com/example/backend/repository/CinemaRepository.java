package com.example.backend.repository;

import com.example.backend.entity.CinemaEntity;
import com.example.backend.entity.RegionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CinemaRepository extends JpaRepository<CinemaEntity, String> {
    
    List<CinemaEntity> findByRegion(RegionEntity region);
}