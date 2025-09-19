package sehwan505.uosticketreservation.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "review",
       indexes = {
           @Index(name = "idx_review_movie", columnList = "movie_id"),
           @Index(name = "idx_review_member", columnList = "user_id"),
           @Index(name = "idx_review_rating", columnList = "rating_value"),
           @Index(name = "idx_review_created", columnList = "created_at")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "review_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id")
    private MovieEntity movie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private MemberEntity member;

    @Column(name = "rating_value")
    private Integer ratingValue; // 1-5점

    @Column(name = "review_content", length = 255)
    private String content;
}