package com.example.backend.controller;

import com.example.backend.dto.MovieDto;
import com.example.backend.dto.MovieSaveDto;
import com.example.backend.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    // 영화 목록 페이지
    @GetMapping
    public String movieList(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.DESC) Pageable pageable,
            Model model) {
        
        Page<MovieDto> movies;
        if (status != null && !status.isEmpty()) {
            movies = movieService.findMoviesByScreeningStatus(status, pageable);
        } else {
            movies = movieService.findAllMovies(pageable);
        }
        
        model.addAttribute("movies", movies);
        model.addAttribute("status", status);
        
        return "movies/list";
    }

    // 영화 상세 페이지
    @GetMapping("/{id}")
    public String movieDetail(@PathVariable Long id, Model model) {
        MovieDto movie = movieService.findMovieById(id);
        model.addAttribute("movie", movie);
        return "movies/detail";
    }

    // 영화 검색 결과 페이지
    @GetMapping("/search")
    public String searchMovies(
            @RequestParam String keyword,
            @PageableDefault(size = 10) Pageable pageable,
            Model model) {
        
        Page<MovieDto> searchResults = movieService.searchMovies(keyword, pageable);
        model.addAttribute("movies", searchResults);
        model.addAttribute("keyword", keyword);
        
        return "movies/search";
    }

    // 영화 등록 폼 (관리자용)
    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String movieForm(Model model) {
        model.addAttribute("movieForm", new MovieSaveDto());
        return "movies/form";
    }

    // 영화 등록 처리 (관리자용)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String saveMovie(@Validated @ModelAttribute("movieForm") MovieSaveDto movieSaveDto,
                            BindingResult result) {
        
        if (result.hasErrors()) {
            return "movies/form";
        }
        
        Long savedId = movieService.saveMovie(movieSaveDto);
        return "redirect:/movies/" + savedId;
    }

    // 영화 수정 폼 (관리자용)
    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String editMovieForm(@PathVariable Long id, Model model) {
        MovieDto movie = movieService.findMovieById(id);
        
        // DTO를 수정용 DTO로 변환
        MovieSaveDto movieForm = MovieSaveDto.builder()
                .title(movie.getTitle())
                .genre(movie.getGenre())
                .releaseDate(movie.getReleaseDate())
                .screeningStatus(movie.getScreeningStatus())
                .runtime(movie.getRuntime())
                .actorName(movie.getActorName())
                .directorName(movie.getDirectorName())
                .distributorName(movie.getDistributorName())
                .viewingGrade(movie.getViewingGrade())
                .description(movie.getDescription())
                .image(movie.getImage())
                .build();
        
        model.addAttribute("movieForm", movieForm);
        model.addAttribute("movieId", id);
        
        return "movies/form";
    }

    // 영화 수정 처리 (관리자용)
    @PostMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String updateMovie(@PathVariable Long id,
                             @Validated @ModelAttribute("movieForm") MovieSaveDto movieSaveDto,
                             BindingResult result) {
        
        if (result.hasErrors()) {
            return "movies/form";
        }
        
        movieService.updateMovie(id, movieSaveDto);
        return "redirect:/movies/" + id;
    }

    // 영화 삭제 (관리자용) - AJAX 요청 처리
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseBody
    public ResponseEntity<String> deleteMovie(@PathVariable Long id) {
        try {
            movieService.deleteMovie(id);
            return ResponseEntity.ok("영화가 삭제되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 인기 영화 API
    @GetMapping("/api/top")
    @ResponseBody
    public List<MovieDto> getTopMovies() {
        return movieService.findTop10ByRating();
    }
}