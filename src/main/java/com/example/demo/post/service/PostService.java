package com.example.demo.post.service;

import com.example.demo.post.entity.Post;
import com.example.demo.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    public Post createPost(String title, String description, String departure, double departureLat, double departureLng,
                           String destination, double destinationLat, double destinationLng,
                           List<String> waypoints, List<Double> waypointLats, List<Double> waypointLngs,
                           String author, MultipartFile imageFile) throws IOException {
        Post post = new Post();
        post.setTitle(title);
        post.setDescription(description);
        post.setDeparture(departure);
        post.setDepartureLat(departureLat);
        post.setDepartureLng(departureLng);
        post.setDestination(destination);
        post.setDestinationLat(destinationLat);
        post.setDestinationLng(destinationLng);
        post.setAuthor(author);

        if (waypoints != null && !waypoints.isEmpty()) {
            for (int i = 0; i < waypoints.size(); i++) {
                String waypoint = waypoints.get(i);
                double waypointLat = waypointLats.get(i);
                double waypointLng = waypointLngs.get(i);
                post.addWaypoint(waypoint, waypointLat, waypointLng);  // Post 엔티티에서 경유지를 추가하는 메서드 호출
            }
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            post.setImage(imageFile.getBytes());
        }

        return postRepository.save(post);
    }

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public Post getPostById(Long id) {
        return postRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid post Id:" + id));
    }

    public Post incrementViewsAndGetPost(Long id) {
        Post post = getPostById(id);
        post.setViews(post.getViews() + 1);
        return postRepository.save(post);
    }

    public Post updatePost(Long id, String title, String description, String departure, double departureLat, double departureLng,
                           String destination, double destinationLat, double destinationLng,
                           List<String> waypoints, List<Double> waypointLats, List<Double> waypointLngs,
                           String author, MultipartFile imageFile) throws IOException {
        Post post = getPostById(id);
        post.setTitle(title);
        post.setDescription(description);
        post.setDeparture(departure);
        post.setDepartureLat(departureLat);
        post.setDepartureLng(departureLng);
        post.setDestination(destination);
        post.setDestinationLat(destinationLat);
        post.setDestinationLng(destinationLng);
        post.setAuthor(author);

        post.getWaypoints().clear();
        post.getWaypointLats().clear();
        post.getWaypointLngs().clear();

        if (waypoints != null && !waypoints.isEmpty()) {
            for (int i = 0; i < waypoints.size(); i++) {
                String waypoint = waypoints.get(i);
                double waypointLat = waypointLats.get(i);
                double waypointLng = waypointLngs.get(i);
                post.addWaypoint(waypoint, waypointLat, waypointLng);
            }
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            post.setImage(imageFile.getBytes());
        }

        return postRepository.save(post);
    }

    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }

    @Value("${custom.kakao.rest.api.key}")
    private String kakaoApiKey;

    public String getRoute(String origin, String destination, String waypoints) {
        RestTemplate restTemplate = new RestTemplate();

        String url = "https://apis-navi.kakaomobility.com/v1/route?" +
                "origin=" + origin + "&destination=" + destination;

        if (waypoints != null && !waypoints.isEmpty()) {
            url += "&waypoints=" + waypoints;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK " + kakaoApiKey);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        return response.getBody();
    }
}