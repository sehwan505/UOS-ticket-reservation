package com.example.backend.service;

import com.example.backend.dto.CinemaDto;
import com.example.backend.dto.CinemaSaveDto;
import com.example.backend.dto.MovieDto;
import com.example.backend.dto.ScheduleDto;
import com.example.backend.entity.CinemaEntity;
import com.example.backend.entity.MovieEntity;
import com.example.backend.entity.RegionEntity;
import com.example.backend.entity.ScheduleEntity;
import com.example.backend.repository.CinemaRepository;
import com.example.backend.repository.RegionRepository;
import com.example.backend.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CinemaService {
    
    private final CinemaRepository cinemaRepository;
    private final RegionRepository regionRepository;
    private final ScheduleRepository scheduleRepository;
    
    // 모든 영화관 조회
    public List<CinemaDto> findAllCinemas() {
        return cinemaRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    // 지역별 영화관 조회
    public List<CinemaDto> findCinemasByRegion(String regionId) {
        RegionEntity region = regionRepository.findById(regionId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지역입니다. ID: " + regionId));
        
        return cinemaRepository.findByRegion(region).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    // 영화관 상세 조회
    public CinemaDto findCinemaById(String id) {
        CinemaEntity cinema = cinemaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 영화관입니다. ID: " + id));
        
        return convertToDto(cinema);
    }
    
    // 특정 영화관에서 스케줄이 있는 상영중인 영화 목록 조회
    public List<MovieDto> findMoviesByCinema(String cinemaId) {
        CinemaEntity cinema = cinemaRepository.findById(cinemaId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 영화관입니다. ID: " + cinemaId));
        
        List<MovieEntity> movies = scheduleRepository.findMoviesByCinema(cinemaId);
        
        return movies.stream()
                .map(this::convertMovieToDto)
                .collect(Collectors.toList());
    }
    
    // 특정 영화관에서 특정 날짜에 스케줄이 있는 상영중인 영화 목록 조회
    public List<MovieDto> findMoviesByCinemaAndDate(String cinemaId, String date) {
        CinemaEntity cinema = cinemaRepository.findById(cinemaId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 영화관입니다. ID: " + cinemaId));
        
        List<MovieEntity> movies = scheduleRepository.findMoviesByCinemaAndDate(cinemaId, date);
        
        return movies.stream()
                .map(this::convertMovieToDto)
                .collect(Collectors.toList());
    }
    
    // 특정 영화관에서 현재 날짜 이후 스케줄이 있는 상영중인 영화 목록 조회
    public List<MovieDto> findCurrentMoviesByCinema(String cinemaId) {
        CinemaEntity cinema = cinemaRepository.findById(cinemaId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 영화관입니다. ID: " + cinemaId));
        
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        List<MovieEntity> movies = scheduleRepository.findCurrentMoviesByCinema(cinemaId, currentDate);
        
        return movies.stream()
                .map(this::convertMovieToDto)
                .collect(Collectors.toList());
    }
    
    // 특정 영화관의 특정 날짜 스케줄 조회
    public List<ScheduleDto> findSchedulesByCinemaAndDate(String cinemaId, String date) {
        CinemaEntity cinema = cinemaRepository.findById(cinemaId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 영화관입니다. ID: " + cinemaId));
        
        List<ScheduleEntity> schedules = scheduleRepository.findSchedulesByCinemaAndDate(cinemaId, date);
        
        return schedules.stream()
                .map(this::convertScheduleToDto)
                .collect(Collectors.toList());
    }
    
    // 특정 영화관의 특정 날짜 범위 스케줄 조회
    public List<ScheduleDto> findSchedulesByCinemaAndDateRange(String cinemaId, String startDate, String endDate) {
        // 영화관 존재 여부 확인
        cinemaRepository.findById(cinemaId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 영화관입니다. ID: " + cinemaId));
        
        List<ScheduleEntity> schedules = scheduleRepository.findSchedulesByCinemaAndDateRange(cinemaId, startDate, endDate);
        
        return schedules.stream()
                .map(this::convertScheduleToDto)
                .collect(Collectors.toList());
    }
    
    // 특정 영화관에서 특정 영화의 모든 스케줄 조회
    public List<ScheduleDto> findSchedulesByCinemaAndMovie(String cinemaId, Long movieId) {
        // 영화관 존재 여부 확인
        cinemaRepository.findById(cinemaId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 영화관입니다. ID: " + cinemaId));
        
        List<ScheduleEntity> schedules = scheduleRepository.findSchedulesByCinemaAndMovie(cinemaId, movieId);
        
        return schedules.stream()
                .map(this::convertScheduleToDto)
                .collect(Collectors.toList());
    }
    
    // 특정 영화관에서 특정 영화의 특정 날짜 스케줄 조회
    public List<ScheduleDto> findSchedulesByCinemaAndMovieAndDate(String cinemaId, Long movieId, String date) {
        // 영화관 존재 여부 확인
        cinemaRepository.findById(cinemaId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 영화관입니다. ID: " + cinemaId));
        
        List<ScheduleEntity> schedules = scheduleRepository.findSchedulesByCinemaAndMovieAndDate(cinemaId, movieId, date);
        
        return schedules.stream()
                .map(this::convertScheduleToDto)
                .collect(Collectors.toList());
    }
    
    // ===== 관리자 기능 =====
    
    // 영화관 등록
    @Transactional
    public String saveCinema(CinemaSaveDto cinemaSaveDto) {
        // 지역 존재 확인
        RegionEntity region = regionRepository.findById(cinemaSaveDto.getRegionId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지역입니다. ID: " + cinemaSaveDto.getRegionId()));
        
        // 영화관 엔티티 생성
        CinemaEntity cinema = CinemaEntity.builder()
                .id(cinemaSaveDto.getId())
                .name(cinemaSaveDto.getName())
                .location(cinemaSaveDto.getLocation())
                .region(region)
                .build();
        
        CinemaEntity savedCinema = cinemaRepository.save(cinema);
        return savedCinema.getId();
    }
    
    // 영화관 정보 수정
    @Transactional
    public String updateCinema(String id, CinemaSaveDto cinemaSaveDto) {
        CinemaEntity cinema = cinemaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 영화관입니다. ID: " + id));
        
        // 지역이 변경되는 경우 지역 존재 확인
        if (!cinema.getRegion().getId().equals(cinemaSaveDto.getRegionId())) {
            RegionEntity newRegion = regionRepository.findById(cinemaSaveDto.getRegionId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 지역입니다. ID: " + cinemaSaveDto.getRegionId()));
            cinema.setRegion(newRegion);
        }
        
        // 영화관 정보 업데이트
        cinema.setName(cinemaSaveDto.getName());
        cinema.setLocation(cinemaSaveDto.getLocation());
        
        return cinema.getId();
    }
    
    // 영화관 삭제
    @Transactional
    public void deleteCinema(String id) {
        CinemaEntity cinema = cinemaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 영화관입니다. ID: " + id));
        
        // 해당 영화관에 상영관이 있는지 확인
        if (!cinema.getScreens().isEmpty()) {
            throw new IllegalStateException("상영관이 존재하는 영화관은 삭제할 수 없습니다.");
        }
        
        cinemaRepository.delete(cinema);
    }
    
    // CinemaEntity를 CinemaDto로 변환
    private CinemaDto convertToDto(CinemaEntity cinema) {
        return CinemaDto.builder()
                .id(cinema.getId())
                .name(cinema.getName())
                .location(cinema.getLocation())
                .regionId(cinema.getRegion().getId())
                .regionName(cinema.getRegion().getName())
                .screenCount(cinema.getScreens().size())
                .build();
    }
    
    // MovieEntity를 MovieDto로 변환
    private MovieDto convertMovieToDto(MovieEntity movie) {
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