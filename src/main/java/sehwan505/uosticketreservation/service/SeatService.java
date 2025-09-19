package sehwan505.uosticketreservation.service;

import sehwan505.uosticketreservation.dto.SeatDto;
import sehwan505.uosticketreservation.dto.SeatSaveDto;
import sehwan505.uosticketreservation.entity.ScreenEntity;
import sehwan505.uosticketreservation.entity.SeatEntity;
import sehwan505.uosticketreservation.entity.SeatGradeEntity;
import sehwan505.uosticketreservation.repository.ScreenRepository;
import sehwan505.uosticketreservation.repository.SeatRepository;
import sehwan505.uosticketreservation.repository.SeatGradeRepository;
import sehwan505.uosticketreservation.constants.StatusConstants;
import sehwan505.uosticketreservation.constants.BusinessConstants;
import sehwan505.uosticketreservation.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeatService {

    private final SeatRepository seatRepository;
    private final ScreenRepository screenRepository;
    private final SeatGradeRepository seatGradeRepository;
    private final IdGenerator idGenerator;

    // 모든 좌석 조회
    public List<SeatDto> findAllSeats() {
        return seatRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // 좌석 상세 조회
    public SeatDto findSeatById(Integer id) {
        SeatEntity seat = seatRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 좌석입니다. ID: " + id));
        return convertToDto(seat);
    }

    // 상영관별 좌석 조회
    public List<SeatDto> findSeatsByScreen(String screenId) {
        ScreenEntity screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상영관입니다. ID: " + screenId));

        return seatRepository.findByScreenOrderByRowAscColumnAsc(screen).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Entity를 DTO로 변환
    private SeatDto convertToDto(SeatEntity seat) {
        return SeatDto.builder()
                .id(seat.getId())
                .seatGradeId(seat.getSeatGrade().getId())
                .seatGradeName(seat.getSeatGrade().getName())
                .row(seat.getRow())
                .column(seat.getColumn())
                .screenId(seat.getScreen().getId())
                .price(seat.getSeatGrade().getPrice())
                .build();
    }

    // 상영관별 좌석 배치도 정보
    public List<List<SeatDto>> getSeatMapByScreen(String screenId) {
        List<SeatDto> seats = findSeatsByScreen(screenId);

        // 행별로 그룹화
        return seats.stream()
                .collect(Collectors.groupingBy(SeatDto::getRow))
                .entrySet().stream()
                .sorted(java.util.Map.Entry.comparingByKey())
                .map(entry -> entry.getValue().stream()
                        .sorted((s1, s2) -> s1.getColumn().compareTo(s2.getColumn()))
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
    }
    
    // ===== 관리자 기능 =====
    
    // 좌석 등록
    @Transactional
    public Integer saveSeat(SeatSaveDto seatSaveDto) {
        // 상영관 존재 확인
        ScreenEntity screen = screenRepository.findById(seatSaveDto.getScreenId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상영관입니다. ID: " + seatSaveDto.getScreenId()));
        
        // 좌석 등급 존재 확인
        SeatGradeEntity seatGrade = seatGradeRepository.findById(seatSaveDto.getSeatGradeId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 좌석 등급입니다. ID: " + seatSaveDto.getSeatGradeId()));
        
        // 동일한 상영관에서 동일한 좌석 위치(행+열) 중복 확인
        boolean exists = seatRepository.existsByScreenAndRowAndColumn(screen, seatSaveDto.getRow(), seatSaveDto.getColumn());
        if (exists) {
            throw new IllegalStateException("해당 위치에 이미 좌석이 존재합니다. (행: " + seatSaveDto.getRow() + ", 열: " + seatSaveDto.getColumn() + ")");
        }
        
        // 좌석 엔티티 생성
        SeatEntity seat = SeatEntity.builder()
                .seatGrade(seatGrade)
                .row(seatSaveDto.getRow())
                .column(seatSaveDto.getColumn())
                .screen(screen)
                .build();
        
        SeatEntity savedSeat = seatRepository.save(seat);
        return savedSeat.getId();
    }
    
    // 좌석 정보 수정
    @Transactional
    public Integer updateSeat(Integer id, SeatSaveDto seatSaveDto) {
        SeatEntity seat = seatRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 좌석입니다. ID: " + id));
        
        // 상영관이 변경되는 경우 상영관 존재 확인
        if (!seat.getScreen().getId().equals(seatSaveDto.getScreenId())) {
            ScreenEntity newScreen = screenRepository.findById(seatSaveDto.getScreenId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상영관입니다. ID: " + seatSaveDto.getScreenId()));
            seat.setScreen(newScreen);
        }
        
        // 좌석 등급이 변경되는 경우 좌석 등급 존재 확인
        if (!seat.getSeatGrade().getId().equals(seatSaveDto.getSeatGradeId())) {
            SeatGradeEntity newSeatGrade = seatGradeRepository.findById(seatSaveDto.getSeatGradeId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 좌석 등급입니다. ID: " + seatSaveDto.getSeatGradeId()));
            seat.setSeatGrade(newSeatGrade);
        }
        
        // 좌석 위치가 변경되는 경우 중복 확인
        if (!seat.getRow().equals(seatSaveDto.getRow()) || !seat.getColumn().equals(seatSaveDto.getColumn())) {
            boolean exists = seatRepository.existsByScreenAndRowAndColumn(seat.getScreen(), seatSaveDto.getRow(), seatSaveDto.getColumn());
            if (exists) {
                throw new IllegalStateException("해당 위치에 이미 좌석이 존재합니다. (행: " + seatSaveDto.getRow() + ", 열: " + seatSaveDto.getColumn() + ")");
            }
        }
        
        // 좌석 정보 업데이트
        seat.setRow(seatSaveDto.getRow());
        seat.setColumn(seatSaveDto.getColumn());
        
        return seat.getId();
    }
    
    // 좌석 삭제
    @Transactional
    public void deleteSeat(Integer id) {
        SeatEntity seat = seatRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 좌석입니다. ID: " + id));
        
        // 해당 좌석에 예약이 있는지 확인
        if (!seat.getReservations().isEmpty()) {
            boolean hasActiveReservations = seat.getReservations().stream()
                    .anyMatch(reservation -> !StatusConstants.Reservation.CANCELLED.equals(reservation.getStatus())); // D = 취소됨
            
            if (hasActiveReservations) {
                throw new IllegalStateException("예약이 존재하는 좌석은 삭제할 수 없습니다.");
            }
        }
        
        seatRepository.delete(seat);
    }
    
    // 상영관의 좌석 일괄 생성 (격자 형태)
    @Transactional
    public List<Integer> createSeatsForScreen(String screenId, String seatGradeId, int rows, int seatsPerRow) {
        // 상영관 존재 확인
        ScreenEntity screen = screenRepository.findById(screenId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상영관입니다. ID: " + screenId));
        
        // 좌석 등급 존재 확인
        SeatGradeEntity seatGrade = seatGradeRepository.findById(seatGradeId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 좌석 등급입니다. ID: " + seatGradeId));
        
        List<SeatEntity> seats = new ArrayList<>();
        
        // A부터 시작하여 행 생성
        for (int i = 0; i < rows; i++) {
            String row = String.valueOf((char) ('A' + i));
            
            // 각 행의 좌석 생성 (01, 02, 03...)
            for (int j = 1; j <= seatsPerRow; j++) {
                String column = idGenerator.generateSeatColumn(j);
                
                // 중복 확인
                boolean exists = seatRepository.existsByScreenAndRowAndColumn(screen, row, column);
                if (!exists) {
                    SeatEntity seat = SeatEntity.builder()
                            .seatGrade(seatGrade)
                            .row(row)
                            .column(column)
                            .screen(screen)
                            .build();
                    seats.add(seat);
                }
            }
        }
        
        List<SeatEntity> savedSeats = seatRepository.saveAll(seats);
        
        // 상영관의 총 좌석 수 업데이트
        screen.setTotalSeats(seatRepository.countByScreen(screen));
        
        return savedSeats.stream()
                .map(SeatEntity::getId)
                .collect(Collectors.toList());
    }
}