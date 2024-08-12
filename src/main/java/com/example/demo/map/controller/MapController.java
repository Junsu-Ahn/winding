package com.example.demo.map.controller;

import com.example.demo.map.DTO.Location;
import com.example.demo.map.DTO.RouteRequest;
import com.example.demo.map.service.MapService;
import com.example.demo.post.service.PostService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

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

    @PostMapping("/route")
    public ResponseEntity<String> calculateRoute(@RequestBody RouteRequest request) {
        // 1. 출발지와 목적지 좌표 가져오기
        String departureCoords = request.getDeparture().getLat() + "," + request.getDeparture().getLng();
        String destinationCoords = request.getDestination().getLat() + "," + request.getDestination().getLng();

        // 2. 경유지 처리
        String waypoints = null;
        if (request.getWaypoints() != null && !request.getWaypoints().isEmpty()) {
            StringBuilder waypointsBuilder = new StringBuilder();
            for (Location waypoint : request.getWaypoints()) {
                waypointsBuilder.append(waypoint.getLat()).append(",").append(waypoint.getLng()).append("|");
            }
            waypoints = waypointsBuilder.toString();
        }

        // 3. 경로 계산 서비스 호출
        String routeData = mapService.getRoute(departureCoords, destinationCoords, waypoints);

        // 4. 경로 데이터를 클라이언트로 반환
        return ResponseEntity.ok(routeData);
    }

    @GetMapping("/geocode")
    @ResponseBody
    public String getGeocode(@RequestParam("address") String address) {
        URI uri = UriComponentsBuilder.fromUriString("https://naveropenapi.apigw.ntruss.com")
                .path("/map-geocode/v2/geocode")
                .queryParam("query", address)
                .build()
                .toUri();

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

            // 응답 로그 추가
            System.out.println("Geocode API response: " + responseBody);

            JsonNode addressesNode = root.path("addresses");
            if (addressesNode.isArray() && addressesNode.size() > 0) {
                JsonNode firstAddress = addressesNode.get(0);
                String longitude = firstAddress.path("x").asText();
                String latitude = firstAddress.path("y").asText();

                return longitude + "," + latitude;
            } else {
                // 유효한 주소가 없는 경우 처리
                System.err.println("No valid addresses found.");
                throw new RuntimeException("No valid addresses found in the response.");
            }
        } catch (Exception e) {
            // 파싱 오류나 기타 예외 처리
            System.err.println("Error parsing the geocode response: " + e.getMessage());
            throw new RuntimeException("Failed to parse the geocode response.", e);
        }
    }
}
