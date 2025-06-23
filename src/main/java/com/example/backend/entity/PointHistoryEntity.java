package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "point_history",
       indexes = {
           @Index(name = "idx_point_history_member", columnList = "user_id")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointHistoryEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "point_history_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private MemberEntity member;

    @Column(name = "point_amount")
    private Integer amount;

    @Column(name = "point_type", length = 1, columnDefinition = "CHAR(1)")
    private String type; // 적립(A)/사용(U)/소멸(E)

    @Column(name = "point_time")
    private LocalDateTime pointTime;

    @PrePersist
    public void prePersist() {
        this.pointTime = LocalDateTime.now();
    }
}