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

    // 썸네일 이미지 필드 추가
    private String thumbnailFilename;   // 썸네일 파일 이름
    private String thumbnailFilepath;   // 썸네일 파일 경로

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

    // 지역 코드를 위한 필드 추가
    private int regionCode;

    // 지역 코드 설정 메서드
    public void assignRegionCode() {
        if (destinationLat >= 37.413294 && destinationLat <= 38.263939 &&
                destinationLng >= 126.764655 && destinationLng <= 127.862730) {
            this.regionCode = 1; // 경기도
        } else if (destinationLat >= 37.008487 && destinationLat <= 38.623477 &&
                destinationLng >= 127.237047 && destinationLng <= 129.305878) {
            this.regionCode = 2; // 강원도
        } else if (destinationLat >= 35.691015 && destinationLat <= 37.003309 &&
                destinationLng >= 128.146118 && destinationLng <= 129.340241) {
            this.regionCode = 3; // 경상북도
        } else if (destinationLat >= 34.564312 && destinationLat <= 35.691015 &&
                destinationLng >= 127.886001 && destinationLng <= 129.321489) {
            this.regionCode = 4; // 경상남도
        } else if (destinationLat >= 34.0167 && destinationLat <= 35.1222 &&
                destinationLng >= 126.1358 && destinationLng <= 127.9984) {
            this.regionCode = 5; // 전라남도
        } else if (destinationLat >= 35.411 && destinationLat <= 36.3664 &&
                destinationLng >= 126.5571 && destinationLng <= 127.7654) {
            this.regionCode = 6; // 전라북도
        } else if (destinationLat >= 36.0163 && destinationLat <= 37.0579 &&
                destinationLng >= 127.4895 && destinationLng <= 128.0889) {
            this.regionCode = 7; // 충청북도
        } else if (destinationLat >= 36.1054 && destinationLat <= 37.0141 &&
                destinationLng >= 126.3251 && destinationLng <= 127.4895) {
            this.regionCode = 8; // 충청남도
        } else {
            this.regionCode = 0; // 기타 지역
        }
    }


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

    // 이미지 및 설명을 위한 리스트 추가
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PostImage> images = new ArrayList<>();

    // 설명을 위한 필드 추가
    @ElementCollection
    @CollectionTable(name = "post_descriptions", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "description")
    private List<String> additionalDescriptions = new ArrayList<>();

    // 이미지 추가 메서드
    public void addImage(PostImage image) {
        this.images.add(image);
        image.setPost(this);
    }

    // 이미지 및 설명 추가 메서드
    public void addImageWithDescription(PostImage image, String description) {
        this.images.add(image);
        this.additionalDescriptions.add(description);
        image.setPost(this);
    }

    // 모든 이미지와 설명 삭제 메서드
    public void clearImages() {
        this.images.clear();
        this.additionalDescriptions.clear();
    }

}


