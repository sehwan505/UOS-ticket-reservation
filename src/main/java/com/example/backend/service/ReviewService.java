package com.example.backend.service;

import com.example.backend.dto.ReviewDto;
import com.example.backend.dto.ReviewSaveDto;
import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.MovieEntity;
import com.example.backend.entity.ReviewEntity;
import com.example.backend.repository.MemberRepository;
import com.example.backend.repository.MovieRepository;
import com.example.backend.repository.ReviewRepository;
import com.example.backend.constants.BusinessConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final MovieRepository movieRepository;
    private final MemberRepository memberRepository;
    
    // 모든 리뷰 조회
    public List<ReviewDto> findAllReviews() {
        return reviewRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    // 영화별 리뷰 조회
    public Page<ReviewDto> findReviewsByMovie(Long movieId, Pageable pageable) {
        MovieEntity movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 영화입니다. ID: " + movieId));
        
        return reviewRepository.findByMovie(movie, pageable)
                .map(this::convertToDto);
    }
    
    // 회원별 리뷰 조회
    public List<ReviewDto> findReviewsByMember(String userId) {
        MemberEntity member = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. ID: " + userId));
        
        List<ReviewEntity> reviews = reviewRepository.findByMember(member);
        return reviews.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    // 리뷰 상세 조회
    public ReviewDto findReviewById(Long id) {
        ReviewEntity review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다. ID: " + id));
        
        return convertToDto(review);
    }
    
    // 리뷰 등록
    @Transactional
    public Long saveReview(ReviewSaveDto reviewSaveDto) {
        MovieEntity movie = movieRepository.findById(reviewSaveDto.getMovieId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 영화입니다. ID: " + reviewSaveDto.getMovieId()));
        
        MemberEntity member = memberRepository.findById(reviewSaveDto.getMemberUserId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다. ID: " + reviewSaveDto.getMemberUserId()));
        
        // 이미 해당 영화에 리뷰를 작성했는지 확인
        Optional<ReviewEntity> existingReview = reviewRepository.findByMemberAndMovie(member, movie);
        if (existingReview.isPresent()) {
            throw new IllegalStateException("이미 이 영화에 리뷰를 작성했습니다.");
        }
        
        ReviewEntity review = ReviewEntity.builder()
                .movie(movie)
                .member(member)
                .ratingValue(reviewSaveDto.getRatingValue())
                .content(reviewSaveDto.getContent())
                .build();
        
        ReviewEntity savedReview = reviewRepository.save(review);
        
        // 영화 평점 업데이트
        updateMovieRating(movie.getId());
        
        return savedReview.getId();
    }
    
    // 리뷰 수정
    @Transactional
    public Long updateReview(Long id, ReviewSaveDto reviewSaveDto) {
        ReviewEntity review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다. ID: " + id));
        
        // 리뷰 수정권한 확인 (Service에서는 생략, Controller에서 처리)
        
        review.setRatingValue(reviewSaveDto.getRatingValue());
        review.setContent(reviewSaveDto.getContent());
        
        // 영화 평점 업데이트
        updateMovieRating(review.getMovie().getId());
        
        return review.getId();
    }
    
    // 리뷰 삭제
    @Transactional
    public void deleteReview(Long id) {
        ReviewEntity review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 리뷰입니다. ID: " + id));
        
        // 리뷰 삭제권한 확인 (Service에서는 생략, Controller에서 처리)
        
        Long movieId = review.getMovie().getId();
        reviewRepository.delete(review);
        
        // 영화 평점 업데이트
        updateMovieRating(movieId);
    }
    
    // 영화 평점 업데이트
    @Transactional
    public void updateMovieRating(Long movieId) {
        MovieEntity movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 영화입니다. ID: " + movieId));
        
        Double avgRating = reviewRepository.calculateAverageRatingForMovie(movieId);
        movie.setRating(avgRating != null ? avgRating : BusinessConstants.Rating.INITIAL_RATING);
        
        movieRepository.save(movie);
    }
    
    // Entity를 DTO로 변환
    private ReviewDto convertToDto(ReviewEntity review) {
        return ReviewDto.builder()
                .id(review.getId())
                .movieId(review.getMovie().getId())
                .movieTitle(review.getMovie().getTitle())
                .memberUserId(review.getMember().getUserId())
                .ratingValue(review.getRatingValue())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}