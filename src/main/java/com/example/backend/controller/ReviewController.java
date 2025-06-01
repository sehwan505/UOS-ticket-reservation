package com.example.backend.controller;

import com.example.backend.dto.MemberDto;
import com.example.backend.dto.MovieDto;
import com.example.backend.dto.ReviewDto;
import com.example.backend.dto.ReviewSaveDto;
import com.example.backend.service.MemberService;
import com.example.backend.service.MovieService;
import com.example.backend.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Review API", description = "영화 리뷰 관리 API")
public class ReviewController {

    private final ReviewService reviewService;
    private final MovieService movieService;
    private final MemberService memberService;

    // 영화별 리뷰 목록
    @GetMapping("/movies/{movieId}")
    @Operation(
        summary = "영화별 리뷰 목록 조회",
        description = "특정 영화의 리뷰 목록을 페이징하여 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "리뷰 목록 조회 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "movie": {
                            "id": 1,
                            "title": "영화 제목",
                            "genre": "액션"
                        },
                        "reviews": {
                            "content": [
                                {
                                    "id": 1,
                                    "rating": 5,
                                    "content": "정말 재미있는 영화였습니다!",
                                    "memberName": "홍길동"
                                }
                            ],
                            "totalElements": 10
                        }
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<Map<String, Object>> getMovieReviews(
            @Parameter(description = "영화 ID", required = true)
            @PathVariable Long movieId,
            @Parameter(description = "페이징 정보")
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
    @Operation(
        summary = "리뷰 등록",
        description = "영화에 대한 리뷰를 등록합니다. 로그인이 필요합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "리뷰 등록 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "id": 1,
                        "message": "리뷰가 등록되었습니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "이미 리뷰 작성함",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "실패 응답",
                    value = """
                    {
                        "error": "alreadyReviewed",
                        "message": "이미 리뷰를 작성하셨습니다."
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<?> createReview(
            @Parameter(description = "영화 ID", required = true)
            @PathVariable Long movieId,
            @Parameter(
                description = "리뷰 정보",
                required = true,
                content = @Content(
                    examples = @ExampleObject(
                        name = "리뷰 등록 요청",
                        value = """
                        {
                            "rating": 5,
                            "content": "정말 재미있는 영화였습니다!"
                        }
                        """
                    )
                )
            )
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
    @Operation(
        summary = "내 리뷰 목록 조회",
        description = "로그인한 사용자의 리뷰 목록을 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "내 리뷰 목록 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ReviewDto.class),
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    [
                        {
                            "id": 1,
                            "movieTitle": "영화 제목",
                            "rating": 5,
                            "content": "정말 재미있는 영화였습니다!",
                            "createdAt": "2024-01-01T10:00:00"
                        }
                    ]
                    """
                )
            )
        )
    })
    public ResponseEntity<List<ReviewDto>> getMyReviews() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        MemberDto member = memberService.findMemberByUserId(auth.getName());
        
        List<ReviewDto> reviews = reviewService.findReviewsByMember(member.getId());
        return ResponseEntity.ok(reviews);
    }

    // 리뷰 상세 조회
    @GetMapping("/{reviewId}")
    @Operation(
        summary = "리뷰 상세 조회",
        description = "특정 리뷰의 상세 정보를 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "리뷰 상세 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ReviewDto.class)
            )
        )
    })
    public ResponseEntity<ReviewDto> getReview(
            @Parameter(description = "리뷰 ID", required = true)
            @PathVariable Long reviewId) {
        ReviewDto review = reviewService.findReviewById(reviewId);
        return ResponseEntity.ok(review);
    }

    // 리뷰 수정
    @PutMapping("/{reviewId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(
        summary = "리뷰 수정",
        description = "본인이 작성한 리뷰를 수정합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "리뷰 수정 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "message": "리뷰가 수정되었습니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "수정 권한 없음",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "권한 없음",
                    value = """
                    {
                        "error": "permission",
                        "message": "수정 권한이 없습니다."
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<?> updateReview(
            @Parameter(description = "리뷰 ID", required = true)
            @PathVariable Long reviewId,
            @Parameter(
                description = "수정할 리뷰 정보",
                required = true,
                content = @Content(
                    examples = @ExampleObject(
                        name = "리뷰 수정 요청",
                        value = """
                        {
                            "rating": 4,
                            "content": "수정된 리뷰 내용입니다."
                        }
                        """
                    )
                )
            )
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
    @Operation(
        summary = "리뷰 삭제",
        description = "본인이 작성한 리뷰를 삭제합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "리뷰 삭제 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "message": "리뷰가 삭제되었습니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "삭제 권한 없음",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "권한 없음",
                    value = """
                    {
                        "error": "삭제 권한이 없습니다."
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<Map<String, String>> deleteReview(
            @Parameter(description = "리뷰 ID", required = true)
            @PathVariable Long reviewId) {
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