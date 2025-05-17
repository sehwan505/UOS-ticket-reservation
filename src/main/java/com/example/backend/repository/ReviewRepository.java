package com.example.backend.repository;

import com.example.backend.entity.Member;
import com.example.backend.entity.Movie;
import com.example.backend.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    
    Page<Review> findByMovie(Movie movie, Pageable pageable);
    
    List<Review> findByMember(Member member);
    
    Optional<Review> findByMemberAndMovie(Member member, Movie movie);
    
    @Query("SELECT AVG(r.ratingValue) FROM Review r WHERE r.movie.id = :movieId")
    Double calculateAverageRatingForMovie(@Param("movieId") Long movieId);
}