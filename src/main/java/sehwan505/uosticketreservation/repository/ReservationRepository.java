package sehwan505.uosticketreservation.repository;

import sehwan505.uosticketreservation.entity.MemberEntity;
import sehwan505.uosticketreservation.entity.NonMemberEntity;
import sehwan505.uosticketreservation.entity.ReservationEntity;
import sehwan505.uosticketreservation.entity.ScheduleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<ReservationEntity, String> {
    
    List<ReservationEntity> findByMember(MemberEntity member);
    
    List<ReservationEntity> findByNonMember(NonMemberEntity nonMember);
    
    List<ReservationEntity> findBySchedule(ScheduleEntity schedule);

    @Query("SELECT r FROM ReservationEntity r WHERE r.schedule.id = :scheduleId AND r.status = 'Y'")
    List<ReservationEntity> findCompletedReservationsByScheduleId(@Param("scheduleId") String scheduleId);
    
    @Query("SELECT r.seat.id FROM ReservationEntity r WHERE r.schedule.id = :scheduleId AND r.status IN ('N', 'Y')")
    List<Integer> findReservedSeatIdsByScheduleId(@Param("scheduleId") String scheduleId);
    
    @Query("SELECT COUNT(r) FROM ReservationEntity r WHERE r.schedule.id = :scheduleId AND r.status = 'Y'")
    Integer countCompletedReservationsByScheduleId(@Param("scheduleId") String scheduleId);
    
    List<ReservationEntity> findByMemberAndStatus(MemberEntity member, String status);
    
    List<ReservationEntity> findByNonMemberAndStatus(NonMemberEntity nonMember, String status);
    
    // 자동 취소를 위한 메서드
    @Query("SELECT r FROM ReservationEntity r WHERE r.status = 'N' AND r.reservationTime < :cutoffTime")
    List<ReservationEntity> findUnpaidReservationsOlderThan(@Param("cutoffTime") LocalDateTime cutoffTime);
    
    @Query("SELECT r FROM ReservationEntity r WHERE r.status = 'N'")
    List<ReservationEntity> findAllUnpaidReservations();
    
    // 배타적 락을 위한 메서드들
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "10000")})
    @Query("SELECT r FROM ReservationEntity r WHERE r.schedule.id = :scheduleId AND r.seat.id = :seatId AND r.status IN ('N', 'Y')")
    Optional<ReservationEntity> findActiveReservationByScheduleAndSeatWithLock(@Param("scheduleId") String scheduleId, @Param("seatId") Integer seatId);
    
    @Query("SELECT COUNT(r) > 0 FROM ReservationEntity r WHERE r.schedule.id = :scheduleId AND r.seat.id = :seatId AND r.status IN ('N', 'Y')")
    boolean existsActiveReservationByScheduleAndSeat(@Param("scheduleId") String scheduleId, @Param("seatId") Integer seatId);
    
    // 스케줄의 모든 활성 예약을 락과 함께 조회
    @Lock(LockModeType.PESSIMISTIC_READ)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "5000")})
    @Query("SELECT r.seat.id FROM ReservationEntity r WHERE r.schedule.id = :scheduleId AND r.status IN ('N', 'Y')")
    List<Integer> findActiveReservedSeatIdsByScheduleIdWithLock(@Param("scheduleId") String scheduleId);
}