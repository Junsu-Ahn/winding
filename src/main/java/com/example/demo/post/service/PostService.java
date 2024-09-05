package com.example.demo.post.service;

import com.example.demo.member.entity.Member;
import com.example.demo.post.entity.Post;
import com.example.demo.post.entity.PostImage;
import com.example.demo.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
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

        // 썸네일 이미지 처리
        if (thumbnail != null && !thumbnail.isEmpty()) {
            String savedFilename = saveImageFile(thumbnail);
            post.setThumbnailFilename(savedFilename);
            post.setThumbnailFilepath("/imagefile/post/" + savedFilename);
        }

        // 추가 이미지 파일 및 설명 처리
        if (imageFiles != null && !imageFiles.isEmpty()) {
            for (int i = 0; i < imageFiles.size(); i++) {
                MultipartFile imageFile = imageFiles.get(i);
                String descriptions = (imageDescriptions != null && i < imageDescriptions.size()) ? imageDescriptions.get(i) : "";

                if (!imageFile.isEmpty()) {
                    String savedFilename = saveImageFile(imageFile);

                    PostImage postImage = new PostImage();
                    postImage.setFilename(savedFilename);
                    postImage.setFilepath("/imagefile/post/" + savedFilename);
                    postImage.setDescriptions(descriptions);

                    post.addImage(postImage);  // 이미지 추가
                }
            }
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

        // 디렉토리 생성
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }

        file.transferTo(dest);  // 파일 저장
        return savedFilename;
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


}