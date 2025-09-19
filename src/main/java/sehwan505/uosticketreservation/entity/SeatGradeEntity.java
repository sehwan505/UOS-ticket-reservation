package sehwan505.uosticketreservation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "seat_grade",
       indexes = {
           @Index(name = "idx_seat_grade_name", columnList = "seat_grade_name"),
           @Index(name = "idx_seat_grade_price", columnList = "seat_price")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatGradeEntity {

    @Id
    @Column(name = "seat_grade_id", length = 1, columnDefinition = "CHAR(1)")
    private String id;

    @Column(name = "seat_grade_name", length = 10)
    private String name;

    @Column(name = "seat_price")
    private Integer price;

    @OneToMany(mappedBy = "seatGrade", cascade = CascadeType.ALL)
    private List<SeatEntity> seats;
}