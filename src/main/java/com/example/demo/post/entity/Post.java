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
    private Member member;

    private String title;
    private String description;

    private String departure;
    private double departureLat;
    private double departureLng;

    private String destination;
    private double destinationLat;
    private double destinationLng;

    private String author;
    private int views = 0;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private List<PostImage> images = new ArrayList<>();

    // 이미지 추가 메서드
    public void addImage(PostImage image) {
        images.add(image);
        image.setPost(this);
    }

    // 이미지 초기화 메서드
    public void clearImages() {
        images.clear();
    }

    // 개별 경유지 필드
    private String waypoint1;
    private Double waypoint1Lat;
    private Double waypoint1Lng;

    private String waypoint2;
    private Double waypoint2Lat;
    private Double waypoint2Lng;

    private String waypoint3;
    private Double waypoint3Lat;
    private Double waypoint3Lng;

    public void setWaypoint(int index, String waypoint, Double lat, Double lng) {
        if (index == 1) {
            this.waypoint1 = waypoint;
            this.waypoint1Lat = lat;
            this.waypoint1Lng = lng;
        } else if (index == 2) {
            this.waypoint2 = waypoint;
            this.waypoint2Lat = lat;
            this.waypoint2Lng = lng;
        } else if (index == 3) {
            this.waypoint3 = waypoint;
            this.waypoint3Lat = lat;
            this.waypoint3Lng = lng;
        }
    }

    public void clearWaypoints() {
        this.waypoint1 = null;
        this.waypoint1Lat = null;
        this.waypoint1Lng = null;

        this.waypoint2 = null;
        this.waypoint2Lat = null;
        this.waypoint2Lng = null;

        this.waypoint3 = null;
        this.waypoint3Lat = null;
        this.waypoint3Lng = null;
    }

    public int getViewCount() {
        return views;
    }

    public void setViewCount(int viewCount) {
        this.views = viewCount;
    }
}

