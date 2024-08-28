package com.example.demo.map.service;

import com.example.demo.map.DTO.Location;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.util.List;

@Service
public class MapService {

    @Value("${custom.naver.api.client.id}")
    private String clientId;

    @Value("${custom.naver.api.client.secret}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 주소를 받아서 Naver Geocoding API를 통해 위도와 경도로 변환하는 메서드
     * @param address 변환할 주소
     * @return 변환된 좌표를 담은 Location 객체
     */
    public Location getCoordinatesAsLocation(String address) {
        String geocodeUrl = "https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode";
        URI uri = UriComponentsBuilder.fromHttpUrl(geocodeUrl)
                .queryParam("query", address)
                .build().encode().toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-NCP-APIGW-API-KEY-ID", clientId);
        headers.set("X-NCP-APIGW-API-KEY", clientSecret);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
        return extractCoordinatesFromResponse(response.getBody());
    }

    /**
     * Geocoding API의 응답에서 위도와 경도를 추출하여 Location 객체로 반환하는 메서드
     * @param responseBody Geocoding API의 응답 본문
     * @return Location 객체 (위도와 경도)
     */
    private Location extractCoordinatesFromResponse(String responseBody) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode addressesNode = root.path("addresses");

            if (addressesNode.isArray() && addressesNode.size() > 0) {
                JsonNode firstAddress = addressesNode.get(0);
                double longitude = firstAddress.path("x").asDouble();
                double latitude = firstAddress.path("y").asDouble();
                return new Location(latitude, longitude);
            } else {
                throw new RuntimeException("No valid addresses found in the response.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse the geocode response.", e);
        }
    }

    /**
     * 출발지, 목적지, 그리고 경유지를 받아서 Naver Directions API를 통해 경로를 계산하는 메서드
     * @param originLat 출발지 위도
     * @param originLng 출발지 경도
     * @param destinationLat 목적지 위도
     * @param destinationLng 목적지 경도
     * @param waypointLats 경유지 위도 리스트
     * @param waypointLngs 경유지 경도 리스트
     * @return 경로 데이터 (JSON 형식)
     */
    public String getRoute(double originLat, double originLng, double destinationLat, double destinationLng, List<Double> waypointLats, List<Double> waypointLngs) {
        String origin = originLng + "," + originLat;
        String destination = destinationLng + "," + destinationLat;

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("https://naveropenapi.apigw.ntruss.com")
                .path("/map-direction/v1/driving")
                .queryParam("start", origin)
                .queryParam("goal", destination)
                .queryParam("option", "trafast");  // 최적 경로 옵션

        // 경유지 추가
        if (waypointLats != null && !waypointLats.isEmpty() && waypointLngs != null && !waypointLngs.isEmpty()) {
            StringBuilder waypointsBuilder = new StringBuilder();
            for (int i = 0; i < waypointLats.size(); i++) {
                if (i > 0) {
                    waypointsBuilder.append("|");
                }
                waypointsBuilder.append(waypointLngs.get(i)).append(",").append(waypointLats.get(i));
            }
            uriBuilder.queryParam("waypoints", waypointsBuilder.toString());
        }

        URI uri = uriBuilder.build().encode().toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-NCP-APIGW-API-KEY-ID", clientId);
        headers.set("X-NCP-APIGW-API-KEY", clientSecret);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);

        return response.getBody();  // JSON 형식의 경로 데이터 반환
    }
}
