package com.example.backend.service;

import com.example.backend.dto.MovieDto;
import com.example.backend.dto.MovieSaveDto;
import com.example.backend.dto.ScheduleDto;
import com.example.backend.entity.MovieEntity;
import com.example.backend.entity.ScheduleEntity;
import com.example.backend.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MovieService {
    
    private final MovieRepository movieRepository;
    
    // 모든 영화 조회
    public Page<MovieDto> findAllMovies(Pageable pageable) {
        return movieRepository.findAll(pageable)
                .map(this::convertToDto);
    }
    
    // 상영 상태별 영화 조회
    public Page<MovieDto> findMoviesByScreeningStatus(String status, Pageable pageable) {
        return movieRepository.findByScreeningStatus(status, pageable)
                .map(this::convertToDto);
    }
    
    // 영화 상세 조회
    public MovieDto findMovieById(Long id) {
        MovieEntity movie = movieRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 영화입니다. ID: " + id));
        return convertToDto(movie);
    }
    
    // 영화 검색
    public Page<MovieDto> searchMovies(String keyword, Pageable pageable) {
        return movieRepository.searchMovies(keyword, pageable)
                .map(this::convertToDto);
    }
    
    // 평점 높은 영화 10개 조회
    public List<MovieDto> findTop10ByRating() {
        return movieRepository.findTop10ByOrderByRatingDesc().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    // 특정 날짜에 상영하는 영화 조회
    public List<MovieDto> findMoviesShowingOnDate(String date) {
        return movieRepository.findMoviesShowingOnDate(date).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    // 영화 등록
    @Transactional
    public Long saveMovie(MovieSaveDto movieSaveDto) {
        MovieEntity movie = MovieEntity.builder()
                .title(movieSaveDto.getTitle())
                .genre(movieSaveDto.getGenre())
                .releaseDate(movieSaveDto.getReleaseDate())
                .screeningStatus(movieSaveDto.getScreeningStatus())
                .runtime(movieSaveDto.getRuntime())
                .actorName(movieSaveDto.getActorName())
                .directorName(movieSaveDto.getDirectorName())
                .distributorName(movieSaveDto.getDistributorName())
                .viewingGrade(movieSaveDto.getViewingGrade())
                .description(movieSaveDto.getDescription())
                .image(movieSaveDto.getImage())
                .rating(0.0) // 새 영화는 평점 0으로 시작
                .build();
        
        MovieEntity savedMovie = movieRepository.save(movie);
        return savedMovie.getId();
    }
    
    // 영화 수정
    @Transactional
    public Long updateMovie(Long id, MovieSaveDto movieSaveDto) {
        MovieEntity movie = movieRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 영화입니다. ID: " + id));
        
        movie.setTitle(movieSaveDto.getTitle());
        movie.setGenre(movieSaveDto.getGenre());
        movie.setReleaseDate(movieSaveDto.getReleaseDate());
        movie.setScreeningStatus(movieSaveDto.getScreeningStatus());
        movie.setRuntime(movieSaveDto.getRuntime());
        movie.setActorName(movieSaveDto.getActorName());
        movie.setDirectorName(movieSaveDto.getDirectorName());
        movie.setDistributorName(movieSaveDto.getDistributorName());
        movie.setViewingGrade(movieSaveDto.getViewingGrade());
        movie.setDescription(movieSaveDto.getDescription());
        movie.setImage(movieSaveDto.getImage());
        
        return movie.getId();
    }
    
    // 영화 삭제
    @Transactional
    public void deleteMovie(Long id) {
        MovieEntity movie = movieRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 영화입니다. ID: " + id));
        
        movieRepository.delete(movie);
    }
    
    // Entity를 DTO로 변환
    private MovieDto convertToDto(MovieEntity movie) {
        return MovieDto.builder()
                .id(movie.getId())
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
                .rating(movie.getRating())
                .build();
    }

    // ScheduleEntity를 ScheduleDto로 변환
    private ScheduleDto convertScheduleToDto(ScheduleEntity schedule) {
        return ScheduleDto.builder()
                .id(schedule.getId())
                .movieId(schedule.getMovie().getId())
                .movieTitle(schedule.getMovie().getTitle())
                .screenId(schedule.getScreen().getId())
                .screenName(schedule.getScreen().getName())
                .screeningDate(schedule.getScreeningDate())
                .screeningStartTime(schedule.getScreeningStartTime())
                .runtime(schedule.getMovie().getRuntime())
                .build();
    }
}