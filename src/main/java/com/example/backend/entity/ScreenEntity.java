package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "screen")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScreenEntity {

    @Id
    @Column(name = "screen_id", length = 2)
    private String id;

    @Column(name = "screen_name", length = 10)
    private String name;

    @Column(name = "total_seats")
    private Integer totalSeats;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cinema_id")
    private CinemaEntity cinema;

    @OneToMany(mappedBy = "screen", cascade = CascadeType.ALL)
    private List<SeatEntity> seats = new ArrayList<>();

    @OneToMany(mappedBy = "screen", cascade = CascadeType.ALL)
    private List<ScheduleEntity> schedules = new ArrayList<>();
}