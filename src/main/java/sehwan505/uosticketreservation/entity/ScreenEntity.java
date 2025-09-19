package sehwan505.uosticketreservation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "screen",
       indexes = {
           @Index(name = "idx_screen_cinema", columnList = "cinema_id, screen_name", unique = true)
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScreenEntity {

    @Id
    @Column(name = "screen_id", length = 4, columnDefinition = "CHAR(4)")
    private String id;

    @Column(name = "screen_name", length = 10)
    private String name;

    @Column(name = "total_seats")
    private Integer totalSeats;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cinema_id")
    private CinemaEntity cinema;

    @OneToMany(mappedBy = "screen", cascade = CascadeType.ALL)
    private List<SeatEntity> seats;

    @OneToMany(mappedBy = "screen", cascade = CascadeType.ALL)
    private List<ScheduleEntity> schedules;
}