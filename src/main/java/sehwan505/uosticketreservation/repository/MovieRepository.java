package sehwan505.uosticketreservation.repository;

import sehwan505.uosticketreservation.entity.MovieEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MovieRepository extends JpaRepository<MovieEntity, Long> {
    
    Page<MovieEntity> findByScreeningStatus(String screeningStatus, Pageable pageable);
    
    @Query("SELECT m FROM MovieEntity m WHERE m.title LIKE %:keyword% OR m.directorName LIKE %:keyword% OR m.actorName LIKE %:keyword%")
    Page<MovieEntity> searchMovies(@Param("keyword") String keyword, Pageable pageable);
    
    List<MovieEntity> findTop10ByOrderByRatingDesc();
    
    @Query("SELECT m FROM MovieEntity m JOIN m.schedules s WHERE s.screeningDate = :date GROUP BY m")
    List<MovieEntity> findMoviesShowingOnDate(@Param("date") String date);
}