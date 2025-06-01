package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "payment")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEntity extends BaseTimeEntity {

    @Id
    @Column(name = "payment_id", length = 36)
    private String id;

    @Column(name = "payment_method", length = 20)
    private String method; // CARD_COMPANY 또는 BANK_COMPANY 형식

    @Column(name = "deducted_points")
    private Integer deductedPoints;

    @Column(name = "payment_amount")
    private Integer amount;

    @Column(name = "payment_time")
    private LocalDateTime paymentTime;

    @Column(name = "payment_status", length = 1)
    private String status; // N: 결제미완료, D: 결제중, Y: 결제완료

    @Column(name = "payment_approval_number", length = 64)
    private String approvalNumber;

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL)
    private List<ReservationEntity> reservations = new ArrayList<>();
}