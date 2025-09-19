package sehwan505.uosticketreservation.repository;

import sehwan505.uosticketreservation.entity.CinemaEntity;
import sehwan505.uosticketreservation.entity.ScreenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScreenRepository extends JpaRepository<ScreenEntity, String> {
    
    List<ScreenEntity> findByCinema(CinemaEntity cinema);
}