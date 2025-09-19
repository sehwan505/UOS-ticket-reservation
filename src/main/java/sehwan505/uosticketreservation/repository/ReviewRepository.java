package sehwan505.uosticketreservation.repository;

import sehwan505.uosticketreservation.entity.MemberEntity;
import sehwan505.uosticketreservation.entity.MovieEntity;
import sehwan505.uosticketreservation.entity.ReviewEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {
    
    Page<ReviewEntity> findByMovie(MovieEntity movie, Pageable pageable);
    
    List<ReviewEntity> findByMember(MemberEntity member);
    
    Optional<ReviewEntity> findByMemberAndMovie(MemberEntity member, MovieEntity movie);
    
    @Query("SELECT AVG(r.ratingValue) FROM ReviewEntity r WHERE r.movie.id = :movieId")
    Double calculateAverageRatingForMovie(@Param("movieId") Long movieId);
}