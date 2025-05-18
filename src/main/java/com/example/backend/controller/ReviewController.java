package com.example.backend.controller;

import com.example.backend.dto.MemberDto;
import com.example.backend.dto.MovieDto;
import com.example.backend.dto.ReviewDto;
import com.example.backend.dto.ReviewSaveDto;
import com.example.backend.service.MemberService;
import com.example.backend.service.MovieService;
import com.example.backend.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final MovieService movieService;
    private final MemberService memberService;

    // 영화별 리뷰 목록
    @GetMapping("/movies/{movieId}")
    public ResponseEntity<Map<String, Object>> getMovieReviews(
            @PathVariable Long movieId,
            @PageableDefault(size = 10) Pageable pageable) {
        
        MovieDto movie = movieService.findMovieById(movieId);
        Page<ReviewDto> reviews = reviewService.findReviewsByMovie(movieId, pageable);
        
        Map<String, Object> response = new HashMap<>();
        response.put("movie", movie);
        response.put("reviews", reviews);
        
        return ResponseEntity.ok(response);
    }

    // 리뷰 등록
    @PostMapping("/movies/{movieId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createReview(
            @PathVariable Long movieId,
            @Valid @RequestBody ReviewSaveDto reviewSaveDto) {
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        MemberDto member = memberService.findMemberByUserId(auth.getName());
        
        try {
            reviewSaveDto.setMovieId(movieId);
            reviewSaveDto.setMemberId(member.getId());
            
            Long reviewId = reviewService.saveReview(reviewSaveDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "id", reviewId,
                    "message", "리뷰가 등록되었습니다."
            ));
            
        } catch (IllegalStateException e) {
            // 이미 리뷰 작성한 경우
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "alreadyReviewed",
                    "message", e.getMessage()
            ));
        }
    }

    // 내 리뷰 목록 조회
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ReviewDto>> getMyReviews() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        MemberDto member = memberService.findMemberByUserId(auth.getName());
        
        List<ReviewDto> reviews = reviewService.findReviewsByMember(member.getId());
        return ResponseEntity.ok(reviews);
    }

    // 리뷰 상세 조회
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewDto> getReview(@PathVariable Long reviewId) {
        ReviewDto review = reviewService.findReviewById(reviewId);
        return ResponseEntity.ok(review);
    }

    // 리뷰 수정
    @PutMapping("/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateReview(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewSaveDto reviewSaveDto) {
        
        ReviewDto review = reviewService.findReviewById(reviewId);
        
        // 권한 확인 (본인 리뷰만 수정 가능)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        MemberDto member = memberService.findMemberByUserId(auth.getName());
        
        if (!review.getMemberId().equals(member.getId()) && !auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                    "error", "permission",
                    "message", "수정 권한이 없습니다."
            ));
        }
        
        reviewSaveDto.setMovieId(review.getMovieId());
        reviewSaveDto.setMemberId(member.getId());
        
        reviewService.updateReview(reviewId, reviewSaveDto);
        return ResponseEntity.ok(Map.of("message", "리뷰가 수정되었습니다."));
    }

    // 리뷰 삭제
    @DeleteMapping("/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> deleteReview(@PathVariable Long reviewId) {
        try {
            ReviewDto review = reviewService.findReviewById(reviewId);
            
            // 권한 확인 (본인 리뷰만 삭제 가능)
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            MemberDto member = memberService.findMemberByUserId(auth.getName());
            
            if (!review.getMemberId().equals(member.getId()) && !auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "삭제 권한이 없습니다."));
            }
            
            reviewService.deleteReview(reviewId);
            return ResponseEntity.ok(Map.of("message", "리뷰가 삭제되었습니다."));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}