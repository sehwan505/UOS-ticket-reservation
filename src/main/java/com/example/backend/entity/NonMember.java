package com.example.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Getter
@Setter
@Table(name = "non_member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class NonMember extends BaseTimeEntity{

    @Id
    @Column(columnDefinition = "CHAR(11)")
    private String phoneNumber;

    @OneToMany(mappedBy = "nonMember", cascade = CascadeType.ALL)
    private List<Reservation> reservations = new ArrayList<>();
}