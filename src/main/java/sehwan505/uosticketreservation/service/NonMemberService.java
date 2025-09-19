package sehwan505.uosticketreservation.service;

import sehwan505.uosticketreservation.entity.NonMemberEntity;
import sehwan505.uosticketreservation.repository.NonMemberRepository;
import sehwan505.uosticketreservation.dto.NonMemberDto;
import sehwan505.uosticketreservation.dto.NonMemberWithReservationsDto;
import sehwan505.uosticketreservation.dto.ReservationDto;
//import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NonMemberService {
    private final NonMemberRepository nonMemberRepository;
    private final ReservationService reservationService;

    @Transactional(readOnly = true)
    public List<NonMemberDto> findAllNonMembers() {
        return nonMemberRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<NonMemberWithReservationsDto> findAllNonMembersWithReservations() {
        List<NonMemberEntity> nonMembers = nonMemberRepository.findAll();
        
        return nonMembers.stream()
                .map(nonMember -> {
                    List<ReservationDto> reservations = reservationService.findReservationsByNonMember(nonMember.getPhoneNumber());
                    return new NonMemberWithReservationsDto(nonMember.getPhoneNumber(), reservations);
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public NonMemberWithReservationsDto findNonMemberWithReservations(String phoneNumber) {
        NonMemberEntity nonMember = nonMemberRepository.findById(phoneNumber)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 비회원입니다. 전화번호: " + phoneNumber));
        
        List<ReservationDto> reservations = reservationService.findReservationsByNonMember(phoneNumber);
        return new NonMemberWithReservationsDto(phoneNumber, reservations);
    }

    private NonMemberDto convertToDto(NonMemberEntity entity) {
        return NonMemberDto.builder()
                .phoneNumber(entity.getPhoneNumber())
                .build();
    }
}