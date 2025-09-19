package sehwan505.uosticketreservation.repository;

import sehwan505.uosticketreservation.entity.SeatGradeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatGradeRepository extends JpaRepository<SeatGradeEntity, String> {
}