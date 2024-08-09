package com.example.demo.map.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class NavigationService {

    @Value("${custom.kakao.rest.api.key}")
    private String kakaoApiKey;

    public String getNavigationRoute(String origin, String destination, String waypoints) {
        RestTemplate restTemplate = new RestTemplate();

        String url = "https://apis-navi.kakaomobility.com/v1/route?origin=" + origin + "&destination=" + destination;

        // waypoints가 빈 문자열인 경우 처리
        if (waypoints != null && !waypoints.trim().isEmpty()) {
            url += "&waypoints=" + waypoints;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoApiKey);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        return response.getBody();
    }
}