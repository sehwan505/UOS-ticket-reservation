package sehwan505.uosticketreservation.repository;

import sehwan505.uosticketreservation.entity.CinemaEntity;
import sehwan505.uosticketreservation.entity.RegionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CinemaRepository extends JpaRepository<CinemaEntity, String> {
    
    List<CinemaEntity> findByRegion(RegionEntity region);
}