package com.example.demo.map.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RouteRequest {
    private Location startLocation;
    private Location goalLocation;
    private List<Location> waypoints;
}

