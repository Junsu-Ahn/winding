package com.example.demo.map.controller;

import com.example.demo.map.service.MapService;
import com.example.demo.post.entity.Post;
import com.example.demo.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    private final PostService postService;

    @GetMapping("/naver-route")
    public ResponseEntity<String> getNaverRoute(
            @RequestParam("start") String start,
            @RequestParam("goal") String goal,
            @RequestParam(name = "waypoints", required = false) String waypoints,
            @RequestParam(name = "option", required = false, defaultValue = "trafast") String option) {

        // 만약 waypoints가 존재하면, 경유지를 포함한 경로 요청
        if (waypoints != null && !waypoints.isEmpty()) {
            System.out.println("경유지를 포함한 경로 요청");
            return mapService.getRouteFromNaver(start, goal, waypoints, option);
        } else {
            // 경유지가 없으면 출발지와 목적지 간의 경로만 요청
            System.out.println("경유지가 없는 경로 요청");
            return mapService.getRouteFromNaver(start, goal, null, option);
        }
    }

    @GetMapping("/posts")
    public ResponseEntity<List<Post>> getAllPosts() {
        List<Post> posts = postService.getAllPosts();
        return ResponseEntity.ok(posts); // posts에 id가 포함되어 있는지 확인
    }
}
