package com.example.demo.map.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Service
public class MapService {
    // 카카오맵 URL을 생성하는 메서드
    @Value("${custom.naver.api.client.id}")
    private String clientId;

    @Value("${custom.naver.api.client.secret}")
    private String clientSecret;

    private final RestTemplate restTemplate = new RestTemplate();

    public String getRoute(String origin, String destination, String waypoints) {
        // 기본 네이버 지도 경로 API의 URI 설정
        URI uri = UriComponentsBuilder.fromUriString("https://naveropenapi.apigw.ntruss.com")
                .path("/map-direction/v1/driving")
                .queryParam("start", origin)
                .queryParam("goal", destination)
                .queryParam("option", "trafast") // 최적 경로 옵션 (trafast: 교통 상황 반영 최적 경로)
                .build()
                .toUri();

        // 경유지가 있는 경우 경유지 추가
        if (waypoints != null && !waypoints.isEmpty()) {
            uri = UriComponentsBuilder.fromUriString(uri.toString())
                    .queryParam("waypoints", waypoints)
                    .build()
                    .toUri();
        }

        // 네이버 API 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-NCP-APIGW-API-KEY-ID", clientId);
        headers.set("X-NCP-APIGW-API-KEY", clientSecret);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        // API 호출 및 응답 처리
        ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, entity, String.class);

        // 결과 반환 (JSON 문자열)
        return response.getBody();
    }
}

