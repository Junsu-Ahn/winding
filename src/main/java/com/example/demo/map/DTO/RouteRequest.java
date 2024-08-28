package com.example.demo.map.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RouteRequest {
    private Location startLocation;  // startAddress를 startLocation으로 변경
    private Location goalLocation;   // goalAddress를 goalLocation으로 변경
    private List<Location> waypoints;  // waypoints 리스트를 List<Location>으로 변경
}

