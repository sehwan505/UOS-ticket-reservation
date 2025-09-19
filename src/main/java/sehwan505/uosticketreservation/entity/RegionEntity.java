package sehwan505.uosticketreservation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "region",
       indexes = {
           @Index(name = "idx_region_name", columnList = "region_name")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegionEntity {

    @Id
    @Column(name = "region_id", length = 2, columnDefinition = "CHAR(2)")
    private String id;

    @Column(name = "region_name", length = 10)
    private String name;

    @OneToMany(mappedBy = "region", cascade = CascadeType.ALL)
    private List<CinemaEntity> cinemas;
}