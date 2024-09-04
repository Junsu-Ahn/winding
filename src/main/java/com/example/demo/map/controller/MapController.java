package com.example.demo.map.controller;

import com.example.demo.map.service.MapService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

}
