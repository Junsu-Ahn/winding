package com.example.demo.post.entity;

import com.example.demo.global.base.BaseEntity;
import com.example.demo.member.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter

public class Post extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member; // Member와의 관계 추가

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String departure;
    private double departureLat;
    private double departureLng;

    private String destination;
    private double destinationLat;
    private double destinationLng;

    private String author; // 작성자
    private int views = 0; // 조회수 초기값은 0

    @Lob
    private byte[] image;

    @ElementCollection
    @CollectionTable(name = "waypoints", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "waypoint")
    private List<String> waypoints = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "waypoint_lats", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "waypoint_lat")
    private List<Double> waypointLats = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "waypoint_lngs", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "waypoint_lng")
    private List<Double> waypointLngs = new ArrayList<>();

    public void addWaypoint(String waypoint, double lat, double lng) {
        this.waypoints.add(waypoint);
        this.waypointLats.add(lat);
        this.waypointLngs.add(lng);
    }
}
