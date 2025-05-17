package com.example.backend.repository;

import com.example.backend.entity.MemberEntity;
import com.example.backend.entity.NonMemberEntity;
import com.example.backend.entity.ReservationEntity;
import com.example.backend.entity.ScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReservationRepository extends JpaRepository<ReservationEntity, String> {
    
    List<ReservationEntity> findByMember(MemberEntity member);
    
    List<ReservationEntity> findByNonMember(NonMemberEntity nonMember);
    
    List<ReservationEntity> findBySchedule(ScheduleEntity schedule);


    @Query("SELECT r FROM ReservationEntity r WHERE r.schedule.id = :scheduleId AND r.status = 'Y'")
    List<ReservationEntity> findCompletedReservationsByScheduleId(@Param("scheduleId") String scheduleId);
    
    @Query("SELECT r.seat.id FROM ReservationEntity r WHERE r.schedule.id = :scheduleId AND r.status = 'Y'")
    List<Integer> findReservedSeatIdsByScheduleId(@Param("scheduleId") String scheduleId);
    
    @Query("SELECT COUNT(r) FROM ReservationEntity r WHERE r.schedule.id = :scheduleId AND r.status = 'Y'")
    Integer countCompletedReservationsByScheduleId(@Param("scheduleId") String scheduleId);
    
    List<ReservationEntity> findByMemberAndStatus(MemberEntity member, String status);
    
    List<ReservationEntity> findByNonMemberAndStatus(NonMemberEntity nonMember, String status);
}