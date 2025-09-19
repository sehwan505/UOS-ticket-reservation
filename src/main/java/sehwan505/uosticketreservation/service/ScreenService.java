package sehwan505.uosticketreservation.service;

import sehwan505.uosticketreservation.dto.ScreenDto;
import sehwan505.uosticketreservation.dto.ScreenSaveDto;
import sehwan505.uosticketreservation.entity.CinemaEntity;
import sehwan505.uosticketreservation.entity.ScreenEntity;
import sehwan505.uosticketreservation.repository.CinemaRepository;
import sehwan505.uosticketreservation.repository.ScreenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ScreenService {
    
    private final ScreenRepository screenRepository;
    private final CinemaRepository cinemaRepository;
    
    // 모든 상영관 조회
    public List<ScreenDto> findAllScreens() {
        return screenRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    // 상영관 상세 조회
    public ScreenDto findScreenById(String id) {
        ScreenEntity screen = screenRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상영관입니다. ID: " + id));
        
        return convertToDto(screen);
    }
    
    // 영화관별 상영관 조회
    public List<ScreenDto> findScreensByCinema(String cinemaId) {
        CinemaEntity cinema = cinemaRepository.findById(cinemaId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 영화관입니다. ID: " + cinemaId));
        
        return screenRepository.findByCinema(cinema).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    // 상영관 등록
    @Transactional
    public String saveScreen(ScreenSaveDto screenSaveDto) {
        // 영화관 존재 확인
        CinemaEntity cinema = cinemaRepository.findById(screenSaveDto.getCinemaId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 영화관입니다. ID: " + screenSaveDto.getCinemaId()));
        
        // 상영관 엔티티 생성
        ScreenEntity screen = ScreenEntity.builder()
                .id(screenSaveDto.getId())
                .name(screenSaveDto.getName())
                .totalSeats(screenSaveDto.getTotalSeats())
                .cinema(cinema)
                .build();
        
        ScreenEntity savedScreen = screenRepository.save(screen);
        return savedScreen.getId();
    }
    
    // 상영관 정보 수정
    @Transactional
    public String updateScreen(String id, ScreenSaveDto screenSaveDto) {
        ScreenEntity screen = screenRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상영관입니다. ID: " + id));
        
        // 영화관이 변경되는 경우 영화관 존재 확인
        if (!screen.getCinema().getId().equals(screenSaveDto.getCinemaId())) {
            CinemaEntity newCinema = cinemaRepository.findById(screenSaveDto.getCinemaId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 영화관입니다. ID: " + screenSaveDto.getCinemaId()));
            screen.setCinema(newCinema);
        }
        
        // 상영관 정보 업데이트
        screen.setName(screenSaveDto.getName());
        screen.setTotalSeats(screenSaveDto.getTotalSeats());
        
        return screen.getId();
    }
    
    // 상영관 삭제
    @Transactional
    public void deleteScreen(String id) {
        ScreenEntity screen = screenRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상영관입니다. ID: " + id));
        
        // 해당 상영관에 스케줄이 있는지 확인
        if (!screen.getSchedules().isEmpty()) {
            throw new IllegalStateException("상영 스케줄이 존재하는 상영관은 삭제할 수 없습니다.");
        }
        
        screenRepository.delete(screen);
    }
    
    // ScreenEntity를 ScreenDto로 변환
    private ScreenDto convertToDto(ScreenEntity screen) {
        return ScreenDto.builder()
                .id(screen.getId())
                .name(screen.getName())
                .totalSeats(screen.getTotalSeats())
                .cinemaId(screen.getCinema().getId())
                .cinemaName(screen.getCinema().getName())
                .build();
    }
} 