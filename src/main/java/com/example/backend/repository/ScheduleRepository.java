package com.example.backend.repository;

import com.example.backend.entity.MovieEntity;
import com.example.backend.entity.ScheduleEntity;
import com.example.backend.entity.ScreenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<ScheduleEntity, String> {
    
    List<ScheduleEntity> findByMovieAndScreeningDate(MovieEntity movie, String date);
    
    List<ScheduleEntity> findByScreenAndScreeningDate(ScreenEntity screen, String date);
    
    @Query("SELECT s FROM ScheduleEntity s WHERE s.movie.id = :movieId AND s.screeningDate = :date ORDER BY s.screeningStartTime")
    List<ScheduleEntity> findByMovieIdAndDateOrderByStartTime(@Param("movieId") Long movieId, @Param("date") String date);
    
    @Query("SELECT DISTINCT s.screeningDate FROM ScheduleEntity s WHERE s.movie.id = :movieId ORDER BY s.screeningDate")
    List<String> findDistinctDatesForMovie(@Param("movieId") Long movieId);
}