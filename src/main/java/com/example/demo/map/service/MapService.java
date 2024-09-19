package com.example.demo.map.service;

import com.example.demo.map.DTO.Location;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

@Service
public class MapService {

    @Value("$custom.fileDirPath")
    private String fileDirPath;
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

    public ResponseEntity<String> getRouteFromNaver(String start, String goal, String waypoints, String option) {

        // UriComponentsBuilder를 사용해 경로 url생성
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl("https://naveropenapi.apigw.ntruss.com/map-direction/v1/driving")
                .queryParam("start", start)
                .queryParam("goal", goal)
                .queryParam("option", option);

        // waypoints가 null이거나 비어있지 않은 경우에만 추가
        if (waypoints != null && !waypoints.isEmpty()) {
            uriBuilder.queryParam("waypoints", waypoints);
        }
        // 네이버 API에 HTTP 요청을 보낼 uri객체 생성
        URI uri = uriBuilder.build().encode().toUri();

        // HTTP 요청의 헤더 객체 생성
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-NCP-APIGW-API-KEY-ID", clientId);
        headers.set("X-NCP-APIGW-API-KEY", clientSecret);

        // entity 객체는 나중에 restTemplate을 통해 실제 HTTP 요청을 보낼 때 사용
        HttpEntity<String> entity = new HttpEntity<>(headers);


        // 네이버 API 서버로 GET 요청을 보내고, 그에 대한 응답을 ResponseEntity<String> 형태로 반환
        return restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);
    }
}
