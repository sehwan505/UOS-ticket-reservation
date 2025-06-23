package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservation", 
       indexes = {
           @Index(name = "idx_reservation_payment", columnList = "payment_id"),
           @Index(name = "idx_reservation_member", columnList = "user_id"),
           @Index(name = "idx_reservation_nonmember", columnList = "phone_number"),
           @Index(name = "idx_reservation_occupy", columnList = "schedule_id, seat_id", unique = true)
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
    @JoinColumn(name = "schedule_id", nullable = false, 
                foreignKey = @ForeignKey(name = "fk_reservation_schedule"))
    private ScheduleEntity schedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id", nullable = false)
    private SeatEntity seat;

    @Column(name = "reservation_status", length = 1, nullable = false, columnDefinition = "CHAR(1)")
    private String status; // N: 예매미완료, D: 예매취소중, Y: 예매완료

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_grade_id")
    private SeatGradeEntity seatGrade;

    @Column(name = "reservation_time", nullable = false)
    private LocalDateTime reservationTime;

    @Column(name = "base_price")
    private Integer basePrice;

    @Column(name = "is_transferred", length = 1, nullable = true, columnDefinition = "CHAR(1)")
    private String isTransferred;

    @Column(name = "discount_code", length = 1, columnDefinition = "CHAR(1)")
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

    @Column(name = "ticket_issuance_status", length = 1, nullable = false, columnDefinition = "CHAR(1)")
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