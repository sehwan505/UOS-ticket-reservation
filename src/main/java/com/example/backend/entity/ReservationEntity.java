package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservation", 
       uniqueConstraints = {
           @UniqueConstraint(
               name = "uk_schedule_seat_active",
               columnNames = {"schedule_id", "seat_id"}
           )
       },
       indexes = {
           @Index(name = "idx_schedule_seat", columnList = "schedule_id, seat_id"),
           @Index(name = "idx_reservation_status", columnList = "reservation_status"),
           @Index(name = "idx_reservation_time", columnList = "reservation_time")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationEntity extends BaseTimeEntity {

    @Id
    @Column(name = "reservation_id", length = 32)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private ScheduleEntity schedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private SeatEntity seat;

    @Column(name = "reservation_status", length = 1, nullable = false)
    private String status; // N: 예매미완료, D: 예매취소중, Y: 예매완료

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_grade_id")
    private SeatGradeEntity seatGrade;

    @Column(name = "reservation_time", nullable = false)
    private LocalDateTime reservationTime;

    @Column(name = "base_price")
    private Integer basePrice;

    @Column(name = "discount_code", length = 1)
    private String discountCode;

    @Column(name = "discount_amount")
    private Integer discountAmount;

    @Column(name = "final_price")
    private Integer finalPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private PaymentEntity payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private MemberEntity member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phone_number")
    private NonMemberEntity nonMember;

    @Column(name = "ticket_issuance_status", length = 1, nullable = false)
    private String ticketIssuanceStatus; // N: 미발권, Y: 발권

    // 예약자가 회원인지 확인
    public boolean isMember() {
        return this.member != null;
    }

    // 할인 적용 메서드
    public void applyDiscount(String discountCode, int discountAmount) {
        this.discountCode = discountCode;
        this.discountAmount = discountAmount;
        calculateFinalPrice();
    }

    // 최종 가격 계산 메서드
    private void calculateFinalPrice() {
        this.finalPrice = this.basePrice - (this.discountAmount != null ? this.discountAmount : 0);
    }
}