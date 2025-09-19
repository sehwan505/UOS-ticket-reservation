package sehwan505.uosticketreservation.repository;

import sehwan505.uosticketreservation.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<PaymentEntity, String> {
    
    PaymentEntity findByApprovalNumber(String approvalNumber);
}