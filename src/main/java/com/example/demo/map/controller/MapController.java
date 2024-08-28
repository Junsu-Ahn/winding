package com.example.demo.map.controller;

import com.example.demo.map.DTO.Location;
import com.example.demo.map.DTO.RouteRequest;
import com.example.demo.map.service.MapService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class MapController {

    @Value("${custom.naver.api.client.id}")
    private String clientId;

    @Value("${custom.naver.api.client.secret}")
    private String clientSecret;

    private final MapService mapService;
    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/get-route")
    public String getRoute(
            @RequestParam(name = "originLat") double originLat,
            @RequestParam(name = "originLng") double originLng,
            @RequestParam(name = "destinationLat") double destinationLat,
            @RequestParam(name = "destinationLng") double destinationLng,
            @RequestParam(name = "waypointLats", required = false) List<Double> waypointLats,
            @RequestParam(name = "waypointLngs", required = false) List<Double> waypointLngs) {

        return mapService.getRoute(originLat, originLng, destinationLat, destinationLng, waypointLats, waypointLngs);
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

    @GetMapping("/naver-route")
    public ResponseEntity<String> getNaverRoute(
            @RequestParam("start") String start,
            @RequestParam("goal") String goal,
            @RequestParam(name = "waypoints", required = false) String waypoints,
            @RequestParam(name = "option", required = false, defaultValue = "trafast") String option) {

        URI uri = UriComponentsBuilder.fromHttpUrl("https://naveropenapi.apigw.ntruss.com/map-direction/v1/driving")
                .queryParam("start", start)
                .queryParam("goal", goal)
                .queryParam("option", option)
                .queryParamIfPresent("waypoints", Optional.ofNullable(waypoints))
                .build().encode().toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-NCP-APIGW-API-KEY-ID", clientId);
        headers.set("X-NCP-APIGW-API-KEY", clientSecret);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("API 요청 중 오류가 발생했습니다.");
        }
    }

    @GetMapping("/geocode")
    public String getGeocode(@RequestParam("address") String address) {
        URI uri = UriComponentsBuilder.fromUriString("https://naveropenapi.apigw.ntruss.com")
                .path("/map-geocode/v2/geocode")
                .queryParam("query", address)
                .build().encode().toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-NCP-APIGW-API-KEY-ID", clientId);
        headers.set("X-NCP-APIGW-API-KEY", clientSecret);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
        return extractCoordinatesFromResponse(response.getBody());
    }

    private String extractCoordinatesFromResponse(String responseBody) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode addressesNode = root.path("addresses");

            if (addressesNode.isArray() && addressesNode.size() > 0) {
                JsonNode firstAddress = addressesNode.get(0);
                double longitude = firstAddress.path("x").asDouble();
                double latitude = firstAddress.path("y").asDouble();
                return longitude + "," + latitude;
            } else {
                throw new RuntimeException("No valid addresses found in the response.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse the geocode response.", e);
        }
    }

    @GetMapping("/naver")
    public Map<String, String> getNaverCredentials() {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("clientId", clientId);
        credentials.put("clientSecret", clientSecret);
        return credentials;
    }
}
