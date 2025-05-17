package com.example.backend.repository;

import com.example.backend.entity.Cinema;
import com.example.backend.entity.Region;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CinemaRepository extends JpaRepository<Cinema, String> {
    
    List<Cinema> findByRegion(Region region);
}