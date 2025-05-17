package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "seat_grade")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatGradeEntity {

    @Id
    @Column(name = "seat_grade_id", length = 1)
    private String id;

    @Column(name = "seat_grade_name", length = 10)
    private String name;

    @Column(name = "seat_price")
    private Integer price;

    @OneToMany(mappedBy = "seatGrade", cascade = CascadeType.ALL)
    private List<SeatEntity> seats = new ArrayList<>();
}