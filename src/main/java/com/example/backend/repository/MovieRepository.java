package com.example.backend.repository;

import com.example.backend.entity.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MovieRepository extends JpaRepository<Movie, Long> {
    
    Page<Movie> findByScreeningStatus(String screeningStatus, Pageable pageable);
    
    @Query("SELECT m FROM Movie m WHERE m.title LIKE %:keyword% OR m.directorName LIKE %:keyword% OR m.actorName LIKE %:keyword%")
    Page<Movie> searchMovies(@Param("keyword") String keyword, Pageable pageable);
    
    List<Movie> findTop10ByOrderByRatingDesc();
    
    @Query("SELECT m FROM Movie m JOIN m.schedules s WHERE s.screeningDate = :date GROUP BY m")
    List<Movie> findMoviesShowingOnDate(@Param("date") String date);
}