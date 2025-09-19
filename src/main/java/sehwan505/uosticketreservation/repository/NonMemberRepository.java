package sehwan505.uosticketreservation.repository;

import sehwan505.uosticketreservation.entity.NonMemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NonMemberRepository extends JpaRepository<NonMemberEntity, String> {
}