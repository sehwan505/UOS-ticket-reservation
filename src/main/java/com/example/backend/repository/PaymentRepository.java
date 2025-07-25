package com.example.backend.repository;

import com.example.backend.entity.PaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<PaymentEntity, String> {
    
    PaymentEntity findByApprovalNumber(String approvalNumber);
}