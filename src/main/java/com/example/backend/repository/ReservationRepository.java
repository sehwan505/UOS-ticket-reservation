package com.example.backend.repository;

import com.example.backend.entity.Member;
import com.example.backend.entity.NonMember;
import com.example.backend.entity.Reservation;
import com.example.backend.entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, String> {
    
    List<Reservation> findByMember(Member member);
    
    List<Reservation> findByNonMember(NonMember nonMember);
    
    List<Reservation> findBySchedule(Schedule schedule);
    
    @Query("SELECT r FROM Reservation r WHERE r.schedule.id = :scheduleId AND r.status = 'Y'")
    List<Reservation> findCompletedReservationsByScheduleId(@Param("scheduleId") String scheduleId);
    
    @Query("SELECT r.seat.id FROM Reservation r WHERE r.schedule.id = :scheduleId AND r.status = 'Y'")
    List<Integer> findReservedSeatIdsByScheduleId(@Param("scheduleId") String scheduleId);
    
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.schedule.id = :scheduleId AND r.status = 'Y'")
    Integer countCompletedReservationsByScheduleId(@Param("scheduleId") String scheduleId);
    
    List<Reservation> findByMemberAndStatus(Member member, String status);
    
    List<Reservation> findByNonMemberAndStatus(NonMember nonMember, String status);
}