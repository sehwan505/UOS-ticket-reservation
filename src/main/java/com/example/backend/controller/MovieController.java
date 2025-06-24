package com.example.backend.controller;

import com.example.backend.dto.MovieDto;
import com.example.backend.dto.MovieSaveDto;
import com.example.backend.service.MovieService;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
@Tag(name = "Movie API", description = "영화 정보 관리 API")
public class MovieController {

    private final MovieService movieService;

    // 영화 목록 조회
    @GetMapping
    @Operation(
        summary = "영화 목록 조회",
        description = "영화 목록을 페이징하여 조회합니다. 상영 상태별 필터링이 가능합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "영화 목록 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Page.class),
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "content": [
                            {
                                "id": 1,
                                "title": "영화 제목",
                                "genre": "액션",
                                "director": "감독명",
                                "rating": 4.5,
                                "screeningStatus": "D"
                            }
                        ],
                        "totalElements": 10,
                        "totalPages": 1
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<Page<MovieDto>> getMovies(
            @Parameter(description = "상영 상태 (Y: 상영중, N: 상영예정, D: 상영종료)")
            @RequestParam(required = false) String status,
            @Parameter(description = "페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10") 
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        
        Page<MovieDto> movies;
        if (status != null && !status.isEmpty()) {
            movies = movieService.findMoviesByScreeningStatus(status, pageable);
        } else {
            movies = movieService.findAllMovies(pageable);
        }
        
        return ResponseEntity.ok(movies);
    }

    // 영화 상세 조회
    @GetMapping("/{id}")
    @Operation(
        summary = "영화 상세 조회",
        description = "특정 영화의 상세 정보를 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "영화 상세 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = MovieDto.class)
            )
        )
    })
    public ResponseEntity<MovieDto> getMovie(
            @Parameter(description = "영화 ID", required = true)
            @PathVariable Long id) {
        MovieDto movie = movieService.findMovieById(id);
        return ResponseEntity.ok(movie);
    }

    // 영화 검색
    @GetMapping("/search")
    @Operation(
        summary = "영화 검색",
        description = "키워드로 영화를 검색합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "영화 검색 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Page.class)
            )
        )
    })
    public ResponseEntity<Page<MovieDto>> searchMovies(
            @Parameter(description = "검색 키워드", required = true)
            @RequestParam String keyword,
            @Parameter(description = "페이징 정보")
            @PageableDefault(size = 10) Pageable pageable) {
        
        Page<MovieDto> searchResults = movieService.searchMovies(keyword, pageable);
        return ResponseEntity.ok(searchResults);
    }

    // 영화 등록 (관리자용)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "영화 등록",
        description = "새로운 영화를 등록합니다. 관리자 권한이 필요합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "영화 등록 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "id": 1
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<Map<String, Long>> createMovie(
            @Parameter(
                description = "영화 정보",
                required = true,
                content = @Content(
                    examples = @ExampleObject(
                        name = "영화 등록 요청",
                        value = """
                        {
                            "title": "영화 제목",
                            "genre": "액션",
                            "director": "감독명",
                            "duration": 120,
                            "description": "영화 설명",
                            "screeningStatus": "D"
                        }
                        """
                    )
                )
            )
            @Valid @RequestBody MovieSaveDto movieSaveDto) {
        Long savedId = movieService.saveMovie(movieSaveDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", savedId));
    }

    // 영화 수정 (관리자용)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "영화 정보 수정",
        description = "영화 정보를 수정합니다. 관리자 권한이 필요합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "영화 수정 성공"
        )
    })
    public ResponseEntity<Void> updateMovie(
            @Parameter(description = "영화 ID", required = true)
            @PathVariable Long id,
            @Parameter(
                description = "수정할 영화 정보",
                required = true,
                content = @Content(
                    examples = @ExampleObject(
                        name = "영화 수정 요청",
                        value = """
                        {
                            "title": "수정된 영화 제목",
                            "genre": "드라마",
                            "director": "감독명",
                            "duration": 130,
                            "description": "수정된 영화 설명",
                            "screeningStatus": "D"
                        }
                        """
                    )
                )
            )
            @Valid @RequestBody MovieSaveDto movieSaveDto) {
        
        movieService.updateMovie(id, movieSaveDto);
        return ResponseEntity.ok().build();
    }

    // 영화 삭제 (관리자용)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
        summary = "영화 삭제",
        description = "영화를 삭제합니다. 관리자 권한이 필요합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "영화 삭제 성공",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    {
                        "message": "영화가 삭제되었습니다."
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "삭제 실패",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "실패 응답",
                    value = """
                    {
                        "error": "삭제할 수 없습니다."
                    }
                    """
                )
            )
        )
    })
    public ResponseEntity<Map<String, String>> deleteMovie(
            @Parameter(description = "영화 ID", required = true)
            @PathVariable Long id) {
        try {
            movieService.deleteMovie(id);
            return ResponseEntity.ok(Map.of("message", "영화가 삭제되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 인기 영화 API
    @GetMapping("/top")
    @Operation(
        summary = "인기 영화 목록",
        description = "평점 기준 상위 10개 인기 영화를 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "인기 영화 목록 조회 성공",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = MovieDto.class),
                examples = @ExampleObject(
                    name = "성공 응답",
                    value = """
                    [
                        {
                            "id": 1,
                            "title": "인기 영화 1",
                            "genre": "액션",
                            "rating": 4.8
                        },
                        {
                            "id": 2,
                            "title": "인기 영화 2",
                            "genre": "드라마",
                            "rating": 4.7
                        }
                    ]
                    """
                )
            )
        )
    })
    public ResponseEntity<List<MovieDto>> getTopMovies() {
        return ResponseEntity.ok(movieService.findTop10ByRating());
    }
}