package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "seat",
       indexes = {
           @Index(name = "idx_seat_identify", columnList = "screen_id, row_id, column_id", unique = true)
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seat_id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_grade_id")
    private SeatGradeEntity seatGrade;

    @Column(name = "row_id", length = 1)
    private String row;

    @Column(name = "column_id", length = 2)
    private String column;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screen_id")
    private ScreenEntity screen;

    @OneToMany(mappedBy = "seat", cascade = CascadeType.ALL)
    private List<ReservationEntity> reservations = new ArrayList<>();

    public String getSeatLabel() {
        return row + column;
    }
}