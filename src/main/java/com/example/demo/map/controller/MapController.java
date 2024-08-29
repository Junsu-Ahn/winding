package com.example.demo.map.controller;

import com.example.demo.map.DTO.Location;
import com.example.demo.map.DTO.RouteRequest;
import com.example.demo.map.service.MapService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MapController {

    @Value("${custom.naver.api.client.id}")
    private String clientId;

    @Value("${custom.naver.api.client.secret}")
    private String clientSecret;

    private final MapService mapService;

    @GetMapping("/naver-route")
    public ResponseEntity<String> getNaverRoute(
            @RequestParam("start") String start,
            @RequestParam("goal") String goal,
            @RequestParam(name = "waypoints", required = false) String waypoints,
            @RequestParam(name = "option", required = false, defaultValue = "trafast") String option) {

        return mapService.getRouteFromNaver(start, goal, waypoints, option);
    }

    @PostMapping("/calculate-route")
    public ResponseEntity<String> calculateRoute(@RequestBody RouteRequest request) {
        try {
            double originLat = request.getStartLocation().getLat();
            double originLng = request.getStartLocation().getLng();
            double destinationLat = request.getGoalLocation().getLat();
            double destinationLng = request.getGoalLocation().getLng();

            List<Double> waypointLats = new ArrayList<>();
            List<Double> waypointLngs = new ArrayList<>();

            if (request.getWaypoints() != null && !request.getWaypoints().isEmpty()) {
                for (Location waypoint : request.getWaypoints()) {
                    waypointLats.add(waypoint.getLat());
                    waypointLngs.add(waypoint.getLng());
                }
            }

            String routeData = mapService.getRoute(originLat, originLng, destinationLat, destinationLng, waypointLats, waypointLngs);
            return ResponseEntity.ok(routeData);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error processing request: " + e.getMessage());
        }
    }

    @GetMapping("/geocode-and-route")
    public ResponseEntity<String> geocodeAndRoute(
            @RequestParam(name = "startAddress") String startAddress,
            @RequestParam(name = "goalAddress") String goalAddress,
            @RequestParam(name = "waypoints", required = false) List<String> waypoints) {

        try {
            Location startLocation = mapService.getCoordinatesAsLocation(startAddress);
            Location goalLocation = mapService.getCoordinatesAsLocation(goalAddress);

            if (startLocation == null || goalLocation == null) {
                return ResponseEntity.badRequest().body("Unable to geocode one or both addresses.");
            }

            List<Double> waypointLats = new ArrayList<>();
            List<Double> waypointLngs = new ArrayList<>();

            if (waypoints != null && !waypoints.isEmpty()) {
                for (String waypoint : waypoints) {
                    Location waypointLocation = mapService.getCoordinatesAsLocation(waypoint);
                    if (waypointLocation != null) {
                        waypointLats.add(waypointLocation.getLat());
                        waypointLngs.add(waypointLocation.getLng());
                    }
                }
            }

            String routeData = mapService.getRoute(
                    startLocation.getLat(),
                    startLocation.getLng(),
                    goalLocation.getLat(),
                    goalLocation.getLng(),
                    waypointLats,
                    waypointLngs
            );
            return ResponseEntity.ok(routeData);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error processing request: " + e.getMessage());
        }
    }
}
