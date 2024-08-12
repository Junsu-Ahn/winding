package com.example.demo.map.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class RouteRequest {
    private Location departure;
    private Location destination;
    private List<Location> waypoints;
}