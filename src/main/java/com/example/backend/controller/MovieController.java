package com.example.backend.controller;

import com.example.backend.dto.MovieDto;
import com.example.backend.dto.MovieSaveDto;
import com.example.backend.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
public class MovieController {

    private final MovieService movieService;

    // 영화 목록 조회
    @GetMapping
    public ResponseEntity<Page<MovieDto>> getMovies(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        
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
    public ResponseEntity<MovieDto> getMovie(@PathVariable Long id) {
        MovieDto movie = movieService.findMovieById(id);
        return ResponseEntity.ok(movie);
    }

    // 영화 검색
    @GetMapping("/search")
    public ResponseEntity<Page<MovieDto>> searchMovies(
            @RequestParam String keyword,
            @PageableDefault(size = 10) Pageable pageable) {
        
        Page<MovieDto> searchResults = movieService.searchMovies(keyword, pageable);
        return ResponseEntity.ok(searchResults);
    }

    // 영화 등록 (관리자용)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> createMovie(@Valid @RequestBody MovieSaveDto movieSaveDto) {
        Long savedId = movieService.saveMovie(movieSaveDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("id", savedId));
    }

    // 영화 수정 (관리자용)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateMovie(@PathVariable Long id, 
                                          @Valid @RequestBody MovieSaveDto movieSaveDto) {
        
        movieService.updateMovie(id, movieSaveDto);
        return ResponseEntity.ok().build();
    }

    // 영화 삭제 (관리자용)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> deleteMovie(@PathVariable Long id) {
        try {
            movieService.deleteMovie(id);
            return ResponseEntity.ok(Map.of("message", "영화가 삭제되었습니다."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // 인기 영화 API
    @GetMapping("/top")
    public ResponseEntity<List<MovieDto>> getTopMovies() {
        return ResponseEntity.ok(movieService.findTop10ByRating());
    }
}