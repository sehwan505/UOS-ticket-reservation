package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cinema",
       indexes = {
           @Index(name = "idx_cinema_region", columnList = "cinema_name, region_id", unique = true)
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CinemaEntity {

    @Id
    @Column(name = "cinema_id", length = 2)
    private String id;

    @Column(name = "cinema_name", length = 10)
    private String name;

    @Column(name = "cinema_location", length = 128)
    private String location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "region_id")
    private RegionEntity region;

    @OneToMany(mappedBy = "cinema", cascade = CascadeType.ALL)
    private List<ScreenEntity> screens = new ArrayList<>();
}