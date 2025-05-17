package com.example.backend.repository;

import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.MovieEntity;
import com.example.backend.entity.ReviewEntity;
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