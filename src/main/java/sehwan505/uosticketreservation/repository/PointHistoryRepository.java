package sehwan505.uosticketreservation.repository;

import sehwan505.uosticketreservation.entity.MemberEntity;
import sehwan505.uosticketreservation.entity.PointHistoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointHistoryRepository extends JpaRepository<PointHistoryEntity, Long> {
    
    Page<PointHistoryEntity> findByMember(MemberEntity member, Pageable pageable);
}