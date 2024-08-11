package com.example.demo.map.controller;

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
    public ResponseEntity<String> calculateRoute(@RequestBody Map<String, String> requestData) {
        // 1. 출발지와 목적지 주소를 받아오기
        String departure = requestData.get("departure");
        String destination = requestData.get("destination");

        // 2. 출발지와 목적지 주소를 네이버 지오코딩 API를 통해 좌표로 변환
        String departureCoords = getGeocode(departure);
        String destinationCoords = getGeocode(destination);

        // 3. 경유지 처리 (필요 시)
        String waypoints = requestData.get("waypoints");

        // 4. 경로 계산 서비스 호출
        String routeData = mapService.getRoute(departureCoords, destinationCoords, waypoints);

        // 5. 경로 데이터를 클라이언트로 반환
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
            // JSON 파서를 위한 ObjectMapper 인스턴스 생성
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(responseBody);

            // 응답의 "addresses" 배열에서 첫 번째 항목을 가져옴
            JsonNode addressesNode = root.path("addresses");
            if (addressesNode.isArray() && addressesNode.size() > 0) {
                JsonNode firstAddress = addressesNode.get(0);

                // 좌표값 추출
                String longitude = firstAddress.path("x").asText();
                String latitude = firstAddress.path("y").asText();

                // "경도,위도" 형식으로 반환
                return longitude + "," + latitude;
            } else {
                // 유효한 주소가 없는 경우 예외 처리
                throw new RuntimeException("No valid addresses found in the response.");
            }
        } catch (Exception e) {
            // 파싱 오류나 기타 예외 처리
            throw new RuntimeException("Failed to parse the geocode response.", e);
        }
    }
}
