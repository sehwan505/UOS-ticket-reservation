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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final MovieService movieService;
    private final MemberService memberService;

    // 영화별 리뷰 목록
    @GetMapping("/movies/{movieId}")
    public String movieReviews(
            @PathVariable Long movieId,
            @PageableDefault(size = 10) Pageable pageable,
            Model model) {
        
        MovieDto movie = movieService.findMovieById(movieId);
        Page<ReviewDto> reviews = reviewService.findReviewsByMovie(movieId, pageable);
        
        model.addAttribute("movie", movie);
        model.addAttribute("reviews", reviews);
        
        return "reviews/movie_reviews";
    }

    // 리뷰 작성 폼
    @GetMapping("/movies/{movieId}/new")
    @PreAuthorize("isAuthenticated()")
    public String reviewForm(@PathVariable Long movieId, Model model) {
        MovieDto movie = movieService.findMovieById(movieId);
        model.addAttribute("movie", movie);
        model.addAttribute("reviewForm", new ReviewSaveDto());
        
        return "reviews/form";
    }

    // 리뷰 등록 처리
    @PostMapping("/movies/{movieId}")
    @PreAuthorize("isAuthenticated()")
    public String saveReview(
            @PathVariable Long movieId,
            @Validated @ModelAttribute("reviewForm") ReviewSaveDto reviewSaveDto,
            BindingResult result,
            Model model) {
        
        if (result.hasErrors()) {
            MovieDto movie = movieService.findMovieById(movieId);
            model.addAttribute("movie", movie);
            return "reviews/form";
        }
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        MemberDto member = memberService.findMemberByUserId(auth.getName());
        
        try {
            reviewSaveDto.setMovieId(movieId);
            reviewSaveDto.setMemberId(member.getId());
            
            Long reviewId = reviewService.saveReview(reviewSaveDto);
            return "redirect:/reviews/movies/" + movieId;
            
        } catch (IllegalStateException e) {
            // 이미 리뷰 작성한 경우
            result.reject("alreadyReviewed", e.getMessage());
            MovieDto movie = movieService.findMovieById(movieId);
            model.addAttribute("movie", movie);
            return "reviews/form";
        }
    }

    // 내 리뷰 목록 조회
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public String myReviews(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        MemberDto member = memberService.findMemberByUserId(auth.getName());
        
        model.addAttribute("reviews", reviewService.findReviewsByMember(member.getId()));
        return "reviews/my_reviews";
    }

    // 리뷰 수정 폼
    @GetMapping("/{reviewId}/edit")
    @PreAuthorize("isAuthenticated()")
    public String editReviewForm(@PathVariable Long reviewId, Model model) {
        ReviewDto review = reviewService.findReviewById(reviewId);
        
        // 권한 확인 (본인 리뷰만 수정 가능)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        MemberDto member = memberService.findMemberByUserId(auth.getName());
        
        if (!review.getMemberId().equals(member.getId()) && !auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new IllegalStateException("수정 권한이 없습니다.");
        }
        
        MovieDto movie = movieService.findMovieById(review.getMovieId());
        
        ReviewSaveDto reviewForm = ReviewSaveDto.builder()
                .ratingValue(review.getRatingValue())
                .content(review.getContent())
                .build();
        
        model.addAttribute("reviewId", reviewId);
        model.addAttribute("movie", movie);
        model.addAttribute("reviewForm", reviewForm);
        
        return "reviews/form";
    }

    // 리뷰 수정 처리
    @PostMapping("/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    public String updateReview(
            @PathVariable Long reviewId,
            @Validated @ModelAttribute("reviewForm") ReviewSaveDto reviewSaveDto,
            BindingResult result,
            Model model) {
        
        ReviewDto review = reviewService.findReviewById(reviewId);
        
        // 권한 확인 (본인 리뷰만 수정 가능)
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        MemberDto member = memberService.findMemberByUserId(auth.getName());
        
        if (!review.getMemberId().equals(member.getId()) && !auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new IllegalStateException("수정 권한이 없습니다.");
        }
        
        if (result.hasErrors()) {
            MovieDto movie = movieService.findMovieById(review.getMovieId());
            model.addAttribute("movie", movie);
            model.addAttribute("reviewId", reviewId);
            return "reviews/form";
        }
        
        reviewSaveDto.setMovieId(review.getMovieId());
        reviewSaveDto.setMemberId(member.getId());
        
        reviewService.updateReview(reviewId, reviewSaveDto);
        return "redirect:/reviews/movies/" + review.getMovieId();
    }

    // 리뷰 삭제
    @DeleteMapping("/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public ResponseEntity<Map<String, String>> deleteReview(@PathVariable Long reviewId) {
        try {
            ReviewDto review = reviewService.findReviewById(reviewId);
            
            // 권한 확인 (본인 리뷰만 삭제 가능)
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            MemberDto member = memberService.findMemberByUserId(auth.getName());
            
            if (!review.getMemberId().equals(member.getId()) && !auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                return ResponseEntity.badRequest().body(Map.of("error", "삭제 권한이 없습니다."));
            }
            
            reviewService.deleteReview(reviewId);
            return ResponseEntity.ok(Map.of("message", "리뷰가 삭제되었습니다."));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}