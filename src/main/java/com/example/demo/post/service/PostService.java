package com.example.demo.post.service;

import com.example.demo.map.service.MapService;
import com.example.demo.member.entity.Member;
import com.example.demo.post.entity.Post;
import com.example.demo.post.entity.PostImage;
import com.example.demo.post.repository.PostRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.mock.web.MockMultipartFile;


import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final MapService mapService;
    @Value("${custom.fileDirPath}")
    private String fileDirPath;

    @Value("${custom.naver.api.client.id}")
    private String clientId;

    @Value("${custom.naver.api.client.secret}")
    private String clientSecret;


    // 게시글 생성 메서드
    public void createPost(String title, String description, String departure, double departureLat,
                           double departureLng, String destination, double destinationLat,
                           double destinationLng, List<String> waypoints, List<Double> waypointLats,
                           List<Double> waypointLngs, String author, MultipartFile thumbnail,
                           List<MultipartFile> imageFiles, List<String> imageDescriptions,
                           Member member) throws IOException {

        // Post 객체 생성
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
        post.setMember(member);

        // 지역 코드 설정
        post.assignRegionCode();  // 목적지 위도, 경도에 따라 지역 코드 할당

        // 썸네일 이미지 처리 (사용자가 직접 업로드한 경우)
        if (thumbnail != null && !thumbnail.isEmpty()) {
            String savedFilename = saveImageFile(thumbnail);
            post.setThumbnailFilename(savedFilename);
            post.setThumbnailFilepath("/imagefile/post/" + savedFilename);
        } else {
            // 썸네일이 없는 경우 지도 캡처본 사용
            String savedFilename = captureMapImage(departureLat, departureLng, destinationLat, destinationLng);
            if (savedFilename != null) {
                post.setThumbnailFilename(savedFilename);
                post.setThumbnailFilepath("/imagefile/post/" + savedFilename);
            }
        }

        // 경유지 처리
        post.clearWaypoints();  // 기존 경유지 정보를 초기화
        if (waypoints != null && !waypoints.isEmpty()) {
            for (int i = 0; i < waypoints.size(); i++) {
                if (i == 3) break;  // 경유지는 최대 3개로 제한
                post.setWaypoint(i + 1, waypoints.get(i), waypointLats.get(i), waypointLngs.get(i));
            }
        }

        // 추가 이미지 파일 및 설명 처리
        if (imageFiles != null && !imageFiles.isEmpty()) {
            System.out.println("받은 이미지 파일 개수: " + imageFiles.size());

            for (int i = 0; i < imageFiles.size(); i++) {
                MultipartFile imageFile = imageFiles.get(i);
                String descriptionForImage = (imageDescriptions != null && i < imageDescriptions.size()) ? imageDescriptions.get(i) : "";

                System.out.println("이미지 파일명: " + imageFile.getOriginalFilename());
                System.out.println("이미지 설명: " + descriptionForImage);

                if (!imageFile.isEmpty()) {
                    String savedFilename = saveImageFile(imageFile);
                    if (savedFilename == null) {
                        System.err.println("이미지 저장 실패: " + imageFile.getOriginalFilename());
                        continue;
                    }

                    // PostImage 객체 생성 및 저장 처리
                    PostImage postImage = new PostImage();
                    postImage.setFilename(savedFilename);
                    postImage.setFilepath("/imagefile/post/" + savedFilename);
                    postImage.setDescriptions(descriptionForImage);
                    postImage.setPost(post);

                    post.addImageWithDescription(postImage, descriptionForImage);
                }
            }
        } else {
            System.out.println("이미지 파일이 전송되지 않았습니다.");
        }

        postRepository.save(post);  // Post 저장
    }


    // 이미지 파일을 저장하는 유틸리티 함수
    private String saveImageFile(MultipartFile file) throws IOException {
        String uuid = UUID.randomUUID().toString();
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String savedFilename = uuid + extension;

        String filePath = Paths.get(fileDirPath, savedFilename).toString();
        File dest = new File(filePath);

        // 디버깅 로그 추가
        System.out.println("파일 저장 경로: " + filePath);

        // 파일 경로가 존재하지 않으면 디렉토리 생성
        if (!dest.getParentFile().exists()) {
            boolean created = dest.getParentFile().mkdirs();
            if (!created) {
                System.err.println("디렉토리 생성 실패: " + dest.getParentFile().getPath());
                return null;
            }
        }

        // 파일 저장
        file.transferTo(dest);
        return savedFilename;
    }

    public String captureMapImage(double departureLat, double departureLng, double destinationLat, double destinationLng) {
        try {
            // 출발지와 도착지 좌표 문자열 생성
            String start = departureLng + "," + departureLat;
            String goal = destinationLng + "," + destinationLat;

            // Directions API를 통해 경로 데이터를 가져옴
            ResponseEntity<String> routeResponse = mapService.getRouteFromNaver(start, goal, null, "trafast");

            // Directions API의 응답에서 경로 좌표를 추출
            String path = extractPathFromDirectionsResponse(routeResponse.getBody());

            if (path == null || path.isEmpty()) {
                System.err.println("경로 데이터를 추출하지 못했습니다.");
                return null;
            }

            // Naver Static Map API 호출 URL 생성
            String apiUrl = "https://naveropenapi.apigw.ntruss.com/map-static/v2/raster?" +
                    "center=" + departureLng + "," + departureLat +
                    "&level=11&w=700&h=500" +
                    "&markers=" + departureLng + "," + departureLat +
                    "|" + destinationLng + "," + destinationLat +
                    "&path=polyline:0xff0000ff:2:" + path;

            // API 호출 시 인증을 위한 헤더 설정
            HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("X-NCP-APIGW-API-KEY-ID", clientId);
            connection.setRequestProperty("X-NCP-APIGW-API-KEY", clientSecret);

            // HTTP 응답 코드 확인
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                // 응답이 성공적일 경우, 이미지 저장 처리
                BufferedImage image = ImageIO.read(connection.getInputStream());

                // BufferedImage를 파일로 저장
                return saveMapImage(image);
            } else {
                System.err.println("API 호출 실패, 응답 코드: " + responseCode);
                return null;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String extractPathFromDirectionsResponse(String responseBody) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode routeNode = root.path("route").path("trafast");

            if (routeNode.isArray() && routeNode.size() > 0) {
                JsonNode firstRoute = routeNode.get(0);
                JsonNode pathNode = firstRoute.path("path");

                StringBuilder pathBuilder = new StringBuilder();

                for (JsonNode point : pathNode) {
                    double longitude = point.get(0).asDouble();
                    double latitude = point.get(1).asDouble();
                    pathBuilder.append(longitude).append(",").append(latitude).append("|");
                }

                // 마지막 파이프(|)를 제거하고 반환
                if (pathBuilder.length() > 0) {
                    pathBuilder.setLength(pathBuilder.length() - 1);
                }

                return pathBuilder.toString();  // 경로 좌표를 파이프(|)로 연결하여 반환
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    // 실제로 이미지를 저장하는 메서드
    private String saveMapImage(BufferedImage image) throws IOException {
        // 고유한 파일 이름 생성 (UUID)
        String uuid = UUID.randomUUID().toString();
        String extension = ".png";  // Naver Static Map API는 PNG 형식을 사용하므로 확장자는 .png
        String savedFilename = uuid + extension;

        // 파일 경로 설정
        String filePath = Paths.get(fileDirPath, savedFilename).toString();
        File outputFile = new File(filePath);

        // 디렉토리 생성
        if (!outputFile.getParentFile().exists()) {
            outputFile.getParentFile().mkdirs();
        }

        // 이미지 파일 저장
        ImageIO.write(image, "png", outputFile);

        return savedFilename;  // 저장된 파일명 반환
    }



    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public Post getPostById(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid post Id:" + id));
    }

    public Post incrementViewsAndGetPost(Long id) {
        Post post = getPostById(id);
        post.setViews(post.getViews() + 1);
        return postRepository.save(post);
    }

    public Post updatePost(Long id, String title, String description, String departure, double departureLat,
                           double departureLng, String destination, double destinationLat,
                           double destinationLng, List<String> waypoints, List<Double> waypointLats,
                           List<Double> waypointLngs, String author, MultipartFile thumbnail,
                           List<MultipartFile> imageFiles, List<String> imageDescriptions) throws IOException {

        // 기존 Post 객체를 가져옴
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

        // 지역 코드 설정
        post.assignRegionCode();  // 목적지 위도, 경도에 따라 지역 코드 할당

        // 기존 경유지 초기화
        post.clearWaypoints();

        // 경유지 업데이트
        if (waypoints != null && !waypoints.isEmpty()) {
            for (int i = 0; i < waypoints.size(); i++) {
                post.setWaypoint(i + 1, waypoints.get(i), waypointLats.get(i), waypointLngs.get(i));
            }
        }

        // 썸네일 이미지 파일 처리
        if (thumbnail != null && !thumbnail.isEmpty()) {
            String uuid = UUID.randomUUID().toString();
            String originalFilename = thumbnail.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String savedThumbnailFilename = uuid + extension; // 썸네일 파일을 위한 변수명 변경
            String filePath = Paths.get(fileDirPath, savedThumbnailFilename).toString();
            File dest = new File(filePath);
            if (!dest.getParentFile().exists()) {
                dest.getParentFile().mkdirs();
            }
            thumbnail.transferTo(dest);
            post.setThumbnailFilename(savedThumbnailFilename);  // 썸네일 파일명 설정
            post.setThumbnailFilepath("/imagefile/post/" + savedThumbnailFilename);  // 썸네일 경로 설정
        }

        // 기존 이미지 초기화
        post.clearImages();

        // 이미지 파일 및 설명 처리
        if (imageFiles != null && !imageFiles.isEmpty()) {
            for (int i = 0; i < imageFiles.size(); i++) {
                MultipartFile imageFile = imageFiles.get(i);
                String descriptions = (imageDescriptions != null && i < imageDescriptions.size()) ? imageDescriptions.get(i) : "";

                if (!imageFile.isEmpty()) {
                    String uuid = UUID.randomUUID().toString();
                    String originalFilename = imageFile.getOriginalFilename();
                    String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                    String savedImageFilename = uuid + extension;  // 이미지 파일 변수명 변경
                    String filePath = Paths.get(fileDirPath, savedImageFilename).toString();
                    File dest = new File(filePath);
                    if (!dest.getParentFile().exists()) {
                        dest.getParentFile().mkdirs();
                    }
                    imageFile.transferTo(dest);

                    PostImage postImage = new PostImage();
                    postImage.setFilename(savedImageFilename);  // 이미지 파일명 설정
                    postImage.setFilepath("/imagefile/post/" + savedImageFilename);  // 파일 경로 설정
                    postImage.setDescriptions(descriptions);  // 설명 설정
                    post.addImage(postImage);  // 이미지 추가
                }
            }
        }

        // Post 업데이트 및 저장
        return postRepository.save(post);
    }


    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }

    public void incrementViewCount(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // 조회수 증가
        post.setViews(post.getViews() + 1);

        // 변경 사항 저장
        postRepository.save(post);
    }

    public List<Post> searchPostsByKeyword(String keyword) {
        return postRepository.findByTitleContainingOrDescriptionContaining(keyword, keyword);
    }

    public List<Post> getPopularPosts() {
        return postRepository.findTop5ByOrderByViewsDesc();
    }

    // 등록일 기준 최신 드라이브 코스 5개 가져오기
    public List<Post> getLatestPosts() {
        return postRepository.findTop5ByOrderByCreateDateDesc();
    }

    public List<Post> getPostsByRegionCode(int regionCode) {
        return postRepository.findByRegionCode(regionCode);
    }
}