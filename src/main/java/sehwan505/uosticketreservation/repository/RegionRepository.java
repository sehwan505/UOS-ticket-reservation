package sehwan505.uosticketreservation.repository;

import sehwan505.uosticketreservation.entity.RegionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RegionRepository extends JpaRepository<RegionEntity, String> {
}