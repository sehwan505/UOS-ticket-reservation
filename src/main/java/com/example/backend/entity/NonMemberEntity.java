package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "non_member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class NonMemberEntity extends BaseTimeEntity{

    @Id
    @Column(columnDefinition = "CHAR(11)")
    private String phoneNumber;

    @OneToMany(mappedBy = "nonMember", cascade = CascadeType.ALL)
    private List<ReservationEntity> reservations = new ArrayList<>();
}