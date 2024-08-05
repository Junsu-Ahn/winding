package com.example.demo.post.entity;

import com.example.demo.global.base.BaseEntity;
import com.example.demo.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Post extends BaseEntity {

    private String title;
    private String author;
    private LocalDate date;
    @Builder.Default
    private int views = 0;
    private String description;
    private String thumbnail;
    private String departure;
    private String destination;
    private double destinationLat;
    private double destinationLng;
    private String waypoint1;
    private String waypoint2;
    private String waypoint3;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;
}
