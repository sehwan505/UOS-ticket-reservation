package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "schedule")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleEntity {

    @Id
    @Column(name = "schedule_id", length = 8)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id")
    private MovieEntity movie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "screen_id")
    private ScreenEntity screen;

    @Column(name = "screening_date", length = 8)
    private String screeningDate;

    @Column(name = "screening_start_time")
    private LocalDateTime screeningStartTime;

    @OneToMany(mappedBy = "schedule", cascade = CascadeType.ALL)
    private List<ReservationEntity> reservations = new ArrayList<>();
}