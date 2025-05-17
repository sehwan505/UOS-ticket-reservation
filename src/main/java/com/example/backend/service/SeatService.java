package com.example.backend.service;

import com.example.backend.dto.SeatDto;
import com.example.backend.entity.ScreenEntity;
import com.example.backend.entity.SeatEntity;
import com.example.backend.repository.ScreenRepository;
import com.example.backend.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SeatService {

    private final SeatRepository seatRepository;
    private final ScreenRepository screenRepository;

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
}