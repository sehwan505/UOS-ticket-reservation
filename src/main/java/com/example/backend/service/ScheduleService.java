package com.example.backend.service;

import com.example.backend.dto.ScheduleDto;
import com.example.backend.dto.ScheduleSaveDto;
import com.example.backend.entity.MovieEntity;
import com.example.backend.entity.ScheduleEntity;
import com.example.backend.entity.ScreenEntity;
import com.example.backend.repository.MovieRepository;
import com.example.backend.repository.ScheduleRepository;
import com.example.backend.repository.ScreenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScheduleService {
    
    private final ScheduleRepository scheduleRepository;
    private final MovieRepository movieRepository;
    private final ScreenRepository screenRepository;
    
    // 모든 상영일정 조회
    public List<ScheduleDto> findAllSchedules() {
        return scheduleRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    // 특정 영화의 상영일정 조회
    public List<ScheduleDto> findSchedulesByMovie(Long movieId, String date) {
        MovieEntity movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 영화입니다. ID: " + movieId));
        
        return scheduleRepository.findByMovieAndScreeningDate(movie, date).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    // 특정 상영관의 상영일정 조회
    public List<ScheduleDto> findSchedulesByScreen(String screenId, String date) {
        ScreenEntity screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상영관입니다. ID: " + screenId));
        
        return scheduleRepository.findByScreenAndScreeningDate(screen, date).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    // 영화별 상영 가능 날짜 조회
    public List<String> findAvailableDatesForMovie(Long movieId) {
        return scheduleRepository.findDistinctDatesForMovie(movieId);
    }
    
    // 영화와 날짜로 상영 시간표 조회
    public List<ScheduleDto> findSchedulesByMovieAndDate(Long movieId, String date) {
        return scheduleRepository.findByMovieIdAndDateOrderByStartTime(movieId, date).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    // 상영일정 상세 조회
    public ScheduleDto findScheduleById(String id) {
        ScheduleEntity schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상영일정입니다. ID: " + id));
        
        return convertToDto(schedule);
    }
    
    // 상영일정 등록
    @Transactional
    public String saveSchedule(ScheduleSaveDto scheduleSaveDto) {
        MovieEntity movie = movieRepository.findById(scheduleSaveDto.getMovieId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 영화입니다. ID: " + scheduleSaveDto.getMovieId()));
        
        ScreenEntity screen = screenRepository.findById(scheduleSaveDto.getScreenId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상영관입니다. ID: " + scheduleSaveDto.getScreenId()));
        
        // 상영시간표번호 생성: YYMMDD + 상영관번호 + 일일상영순서
        String date = scheduleSaveDto.getScreeningDate();
        String yearMonth = date.substring(2, 6); // YYMM
        String day = date.substring(6, 8); // DD
        
        // 일일상영순서 계산 (같은 날짜, 같은 상영관에 몇 번째 상영인지)
        int dailyOrder = scheduleRepository.findByScreenAndScreeningDate(screen, date).size() + 1;
        
        // 상영시간표번호 형식: YYMMDD + 상영관번호 + 일일상영순서(2자리)
        String scheduleId = yearMonth + day + screen.getId() + String.format("%02d", dailyOrder);
        
        // 상영시작시간 파싱
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        LocalDateTime startTime = LocalDateTime.parse(date + scheduleSaveDto.getScreeningStartTime(), formatter);
        
        ScheduleEntity schedule = ScheduleEntity.builder()
                .id(scheduleId)
                .movie(movie)
                .screen(screen)
                .screeningDate(date)
                .screeningStartTime(startTime)
                .build();
        
        ScheduleEntity savedSchedule = scheduleRepository.save(schedule);
        return savedSchedule.getId();
    }
    
    // 상영일정 수정
    @Transactional
    public String updateSchedule(String id, ScheduleSaveDto scheduleSaveDto) {
        ScheduleEntity schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상영일정입니다. ID: " + id));
        
        MovieEntity movie = movieRepository.findById(scheduleSaveDto.getMovieId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 영화입니다. ID: " + scheduleSaveDto.getMovieId()));
        
        ScreenEntity screen = screenRepository.findById(scheduleSaveDto.getScreenId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상영관입니다. ID: " + scheduleSaveDto.getScreenId()));
        
        // 상영시작시간 파싱
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        LocalDateTime startTime = LocalDateTime.parse(scheduleSaveDto.getScreeningDate() + scheduleSaveDto.getScreeningStartTime(), formatter);
        
        schedule.setMovie(movie);
        schedule.setScreen(screen);
        schedule.setScreeningDate(scheduleSaveDto.getScreeningDate());
        schedule.setScreeningStartTime(startTime);
        
        return schedule.getId();
    }
    
    // 상영일정 삭제
    @Transactional
    public void deleteSchedule(String id) {
        ScheduleEntity schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상영일정입니다. ID: " + id));
        
        scheduleRepository.delete(schedule);
    }
    
    // Entity를 DTO로 변환
    private ScheduleDto convertToDto(ScheduleEntity schedule) {
        return ScheduleDto.builder()
                .id(schedule.getId())
                .movieId(schedule.getMovie().getId())
                .movieTitle(schedule.getMovie().getTitle())
                .screenId(schedule.getScreen().getId())
                .screenName(schedule.getScreen().getName())
                .cinemaName(schedule.getScreen().getCinema().getName())
                .screeningDate(schedule.getScreeningDate())
                .screeningStartTime(schedule.getScreeningStartTime())
                .runtime(schedule.getMovie().getRuntime())
                .build();
    }
}