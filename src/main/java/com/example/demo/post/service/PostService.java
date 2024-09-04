package com.example.demo.post.service;

import com.example.demo.member.entity.Member;
import com.example.demo.post.entity.Post;
import com.example.demo.post.entity.PostImage;
import com.example.demo.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    @Value("${custom.fileDirPath}")
    private String fileDirPath;

    public void createPost(String title, String description, String departure, double departureLat,
                           double departureLng, String destination, double destinationLat,
                           double destinationLng, List<String> waypoints, List<Double> waypointLats,
                           List<Double> waypointLngs, String author, List<MultipartFile> imageFiles,
                           List<String> imageDescriptions, Member member) throws IOException {

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

        // 이미지 파일 처리
        for (int i = 0; i < imageFiles.size(); i++) {
            MultipartFile imageFile = imageFiles.get(i);
            String imageDescription = imageDescriptions.get(i);

            if (!imageFile.isEmpty()) {
                String originalFilename = imageFile.getOriginalFilename();
                String newFilename = UUID.randomUUID().toString() + "_" + originalFilename;
                Path imagePath = Paths.get(fileDirPath, newFilename);
                Files.createDirectories(imagePath.getParent());
                Files.write(imagePath, imageFile.getBytes());

                PostImage postImage = new PostImage();
                postImage.setFilename(originalFilename);
                postImage.setFilepath("/imagefile/post/" + newFilename);
                postImage.setDescription(imageDescription);

                post.addImage(postImage);
            }
        }

        // 경유지 설정
        if (waypoints != null && !waypoints.isEmpty()) {
            for (int i = 0; i < waypoints.size(); i++) {
                post.setWaypoint(i + 1, waypoints.get(i), waypointLats.get(i), waypointLngs.get(i));
            }
        }

        postRepository.save(post);
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

    public Post updatePost(Long id, String title, String description, String departure, double departureLat, double departureLng,
                           String destination, double destinationLat, double destinationLng,
                           List<String> waypoints, List<Double> waypointLats, List<Double> waypointLngs,
                           String author, List<MultipartFile> imageFiles, List<String> imageDescriptions) throws IOException {
        Post post = getPostById(id);  // 기존 Post 객체를 가져옴
        post.setTitle(title);
        post.setDescription(description);
        post.setDeparture(departure);
        post.setDepartureLat(departureLat);
        post.setDepartureLng(departureLng);
        post.setDestination(destination);
        post.setDestinationLat(destinationLat);
        post.setDestinationLng(destinationLng);
        post.setAuthor(author);

        // 기존 이미지와 경유지 초기화
        post.clearImages();  // 기존 이미지를 초기화합니다.
        post.clearWaypoints();  // 기존 경유지를 초기화합니다.

        // 경유지 업데이트
        if (waypoints != null && !waypoints.isEmpty()) {
            for (int i = 0; i < waypoints.size(); i++) {
                post.setWaypoint(i + 1, waypoints.get(i), waypointLats.get(i), waypointLngs.get(i));
            }
        }

        // 이미지 파일 처리
        if (imageFiles != null && !imageFiles.isEmpty()) {
            for (int i = 0; i < imageFiles.size(); i++) {
                MultipartFile imageFile = imageFiles.get(i);
                String imageDescription = imageDescriptions != null && imageDescriptions.size() > i ? imageDescriptions.get(i) : "";

                if (!imageFile.isEmpty()) {
                    String originalFilename = imageFile.getOriginalFilename();
                    String newFilename = UUID.randomUUID().toString() + "_" + originalFilename;
                    Path imagePath = Paths.get(fileDirPath, newFilename);
                    Files.createDirectories(imagePath.getParent());
                    Files.write(imagePath, imageFile.getBytes());

                    PostImage postImage = new PostImage();
                    postImage.setFilename(originalFilename);
                    postImage.setFilepath("/imagefile/post/" + newFilename);
                    postImage.setDescription(imageDescription);

                    post.addImage(postImage);
                }
            }
        }


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


}