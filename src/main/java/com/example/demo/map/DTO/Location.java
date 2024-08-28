package com.example.demo.map.DTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Location {
    private double lat;
    private double lng;

    // 기본 생성자
    public Location() {}

    // 위도와 경도를 받는 생성자 추가
    public Location(double lat, double lng) {
        this.lat = lat;
        this.lng = lng;
    }

    // Getter와 Setter
    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}