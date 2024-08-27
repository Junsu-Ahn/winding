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
import java.util.HashMap;
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

    // 기존 경로 계산 메서드
    @PostMapping("/route")
    public ResponseEntity<String> calculateRoute(@RequestBody RouteRequest request) {
        String departureCoords = request.getDeparture().getLat() + "," + request.getDeparture().getLng();
        String destinationCoords = request.getDestination().getLat() + "," + request.getDestination().getLng();

        String waypoints = null;
        if (request.getWaypoints() != null && !request.getWaypoints().isEmpty()) {
            StringBuilder waypointsBuilder = new StringBuilder();
            for (Location waypoint : request.getWaypoints()) {
                waypointsBuilder.append(waypoint.getLat()).append(",").append(waypoint.getLng()).append("|");
            }
            waypoints = waypointsBuilder.toString();
        }

        String routeData = mapService.getRoute(departureCoords, destinationCoords, waypoints);
        return ResponseEntity.ok(routeData);
    }

    // 네이버 API로부터 경로 데이터를 프록시하는 메서드
    @GetMapping("/naver-route")
    public ResponseEntity<String> getNaverRoute(
            @RequestParam("start") String start,
            @RequestParam("goal") String goal,
            @RequestParam(name = "waypoints", required = false) String waypoints,
            @RequestParam(name = "option", required = false, defaultValue = "trafast") String option) {

        // URI 빌더를 사용하여 API 요청 URI 구성
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl("https://naveropenapi.apigw.ntruss.com/map-direction/v1/driving")
                .queryParam("start", start)
                .queryParam("goal", goal)
                .queryParam("option", option);

        if (waypoints != null && !waypoints.isEmpty()) {
            uriBuilder.queryParam("waypoints", waypoints);
        }

        URI uri = uriBuilder.build().encode().toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-NCP-APIGW-API-KEY-ID", clientId);
        headers.set("X-NCP-APIGW-API-KEY", clientSecret);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            // API 요청 전송 및 응답 처리
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
            System.out.println("Response Status Code: " + response.getStatusCode());
            System.out.println("Response Body: " + response.getBody());
            return ResponseEntity.ok(response.getBody());
        } catch (Exception e) {
            // 예외 발생 시 로그 출력 및 500 상태 반환
            e.printStackTrace();
            return ResponseEntity.status(500).body("API 요청 중 오류가 발생했습니다.");
        }
    }


    // 기존의 지오코딩 메서드
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

            JsonNode addressesNode = root.path("addresses");
            if (addressesNode.isArray() && addressesNode.size() > 0) {
                JsonNode firstAddress = addressesNode.get(0);
                String longitude = firstAddress.path("x").asText();
                String latitude = firstAddress.path("y").asText();

                return longitude + "," + latitude;
            } else {
                throw new RuntimeException("No valid addresses found in the response.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse the geocode response.", e);
        }
    }

    // 네이버 API 키를 프론트엔드에 제공하는 메서드
    @GetMapping("/naver")
    public Map<String, String> getNaverCredentials() {
        Map<String, String> credentials = new HashMap<>();
        credentials.put("clientId", clientId);
        credentials.put("clientSecret", clientSecret);
        return credentials;
    }
}
