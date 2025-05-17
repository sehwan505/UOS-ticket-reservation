package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "reservation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reservation extends BaseTimeEntity {

    @Id
    @Column(name = "reservation_id", length = 14)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id")
    private Schedule schedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_id")
    private Seat seat;

    @Column(name = "reservation_status", length = 1)
    private String status; // N: 예매미완료, D: 예매취소중, Y: 예매완료

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_grade_id")
    private SeatGrade seatGrade;

    @Column(name = "reservation_time")
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
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "phone_number")
    private NonMember nonMember;

    @Column(name = "ticket_issuance_status", length = 1)
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