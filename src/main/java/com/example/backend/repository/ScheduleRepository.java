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

    @Query("SELECT DISTINCT s.movie FROM ScheduleEntity s WHERE s.screen.cinema.id = :cinemaId AND s.movie.screeningStatus = 'D'")
    List<MovieEntity> findMoviesByCinema(@Param("cinemaId") String cinemaId);

    @Query("SELECT DISTINCT s.movie FROM ScheduleEntity s WHERE s.screen.cinema.id = :cinemaId AND s.screeningDate = :date AND s.movie.screeningStatus = 'D'")
    List<MovieEntity> findMoviesByCinemaAndDate(@Param("cinemaId") String cinemaId, @Param("date") String date);

    @Query("SELECT DISTINCT s.movie FROM ScheduleEntity s WHERE s.screen.cinema.id = :cinemaId AND s.screeningDate >= :currentDate AND s.movie.screeningStatus = 'D'")
    List<MovieEntity> findCurrentMoviesByCinema(@Param("cinemaId") String cinemaId, @Param("currentDate") String currentDate);

    @Query("SELECT s FROM ScheduleEntity s WHERE s.screen.cinema.id = :cinemaId AND s.screeningDate = :date ORDER BY s.screeningStartTime")
    List<ScheduleEntity> findSchedulesByCinemaAndDate(@Param("cinemaId") String cinemaId, @Param("date") String date);

    @Query("SELECT s FROM ScheduleEntity s WHERE s.screen.cinema.id = :cinemaId AND s.screeningDate BETWEEN :startDate AND :endDate ORDER BY s.screeningDate, s.screeningStartTime")
    List<ScheduleEntity> findSchedulesByCinemaAndDateRange(@Param("cinemaId") String cinemaId, @Param("startDate") String startDate, @Param("endDate") String endDate);
    
    // 특정 영화관에서 특정 영화의 모든 스케줄 조회
    @Query("SELECT s FROM ScheduleEntity s WHERE s.screen.cinema.id = :cinemaId AND s.movie.id = :movieId ORDER BY s.screeningDate, s.screeningStartTime")
    List<ScheduleEntity> findSchedulesByCinemaAndMovie(@Param("cinemaId") String cinemaId, @Param("movieId") Long movieId);
    
    // 특정 영화관에서 특정 영화의 특정 날짜 스케줄 조회
    @Query("SELECT s FROM ScheduleEntity s WHERE s.screen.cinema.id = :cinemaId AND s.movie.id = :movieId AND s.screeningDate = :date ORDER BY s.screeningStartTime")
    List<ScheduleEntity> findSchedulesByCinemaAndMovieAndDate(@Param("cinemaId") String cinemaId, @Param("movieId") Long movieId, @Param("date") String date);
}