package com.example.backend.repository;

import com.example.backend.entity.Movie;
import com.example.backend.entity.Schedule;
import com.example.backend.entity.Screen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule, String> {
    
    List<Schedule> findByMovieAndScreeningDate(Movie movie, String date);
    
    List<Schedule> findByScreenAndScreeningDate(Screen screen, String date);
    
    @Query("SELECT s FROM Schedule s WHERE s.movie.id = :movieId AND s.screeningDate = :date ORDER BY s.screeningStartTime")
    List<Schedule> findByMovieIdAndDateOrderByStartTime(@Param("movieId") Long movieId, @Param("date") String date);
    
    @Query("SELECT DISTINCT s.screeningDate FROM Schedule s WHERE s.movie.id = :movieId ORDER BY s.screeningDate")
    List<String> findDistinctDatesForMovie(@Param("movieId") Long movieId);
}