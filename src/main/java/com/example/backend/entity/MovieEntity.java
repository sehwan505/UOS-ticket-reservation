package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "movie")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Movie extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "movie_id")
    private Long id;

    @Column(name = "movie_title", nullable = false, length = 128)
    private String title;

    @Column(name = "movie_genre", length = 2)
    private String genre;

    @Column(name = "release_date", length = 8)
    private String releaseDate;

    @Column(name = "screening_status", length = 1)
    private String screeningStatus; // N: 상영전, D: 상영중, Y: 상영안함

    @Column(name = "runtime")
    private Integer runtime; // 분 단위

    @Column(name = "actor_name", length = 20)
    private String actorName;

    @Column(name = "director_name", length = 20)
    private String directorName;

    @Column(name = "distributor_name", length = 40)
    private String distributorName;

    @Column(name = "viewing_grade", length = 6)
    private String viewingGrade; // 전체/12/15/19 등

    @Column(name = "movie_description", length = 256)
    private String description;

    @Column(name = "movie_image", length = 255)
    private String image;

    @Column(name = "movie_rating", precision = 3, scale = 2)
    private Double rating;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Schedule> schedules = new ArrayList<>();
    
    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    // 평점 업데이트 메서드
    public void updateRating() {
        if (reviews.isEmpty()) {
            this.rating = 0.0;
            return;
        }
        
        double sum = reviews.stream()
                .mapToInt(Review::getRatingValue)
                .sum();
        this.rating = sum / reviews.size();
    }
}