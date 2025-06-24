package com.example.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "member",
       indexes = {
           @Index(name = "idx_member_email", columnList = "email_address")
       })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberEntity extends BaseTimeEntity implements UserDetails {

    @Id
    @Column(name = "user_id", length = 20, unique = true)
    private String userId;

    @Column(name = "password", length = 70)
    private String password;

    @Column(name = "email_address", length = 64)
    private String email;

    @Column(name = "phone_number", length = 11)
    private String phoneNumber;

    @Column(name = "birth_date", length = 8, columnDefinition = "CHAR(8)")
    private String birthDate;

    @Column(name = "member_grade", length = 1, columnDefinition = "CHAR(1)")
    private String grade;

    @Column(name = "available_points")
    private Integer availablePoints;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<PointHistoryEntity> pointHistories;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<ReviewEntity> reviews;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<ReservationEntity> reservations;

    // UserDetails 구현 메서드
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // grade가 "A"인 경우 관리자 권한 부여
        if ("A".equals(this.grade)) {
            return List.of(
                new SimpleGrantedAuthority("ROLE_ADMIN"),
                new SimpleGrantedAuthority("ROLE_USER")
            );
        }
        return Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getUsername() {
        return this.userId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}