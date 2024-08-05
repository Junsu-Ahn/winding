package com.example.demo.post.service;

import com.example.demo.map.service.MapService;
import com.example.demo.member.entity.Member;
import com.example.demo.member.repository.MemberRepository;
import com.example.demo.post.entity.Post;
import com.example.demo.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final MapService mapService;
    private final String fileDirPath = "/path/to/upload/directory";

    public Post createPost(String title, String description, String departure, String destination, double destinationLat, double destinationLng, String waypoint1, String waypoint2, String waypoint3, Long memberId, MultipartFile thumbnail) {
        String mapUrl = mapService.getMapUrl(destination);

        String thumbnailRelPath = "post/" + UUID.randomUUID().toString() + ".jpg";
        File thumbnailFile = new File(fileDirPath + "/" + thumbnailRelPath);

        try {
            if (!thumbnailFile.getParentFile().exists()) {
                thumbnailFile.getParentFile().mkdirs();
            }
            thumbnail.transferTo(thumbnailFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Member member = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException("Member not found: " + memberId));

        Post post = Post.builder()
                .title(title)
                .description(description)
                .departure(departure)
                .destination(destination)
                .destinationLat(destinationLat)
                .destinationLng(destinationLng)
                .waypoint1(waypoint1)
                .waypoint2(waypoint2)
                .waypoint3(waypoint3)
                .thumbnail(thumbnailRelPath)
                .member(member)
                .build();

        return postRepository.save(post);
    }

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }

    public Post getPostById(Long id) {
        return postRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Post not found: " + id));
    }

    public Post updatePost(Long id, String title, String description, String departure, String destination, double destinationLat, double destinationLng, String waypoint1, String waypoint2, String waypoint3, Long memberId, MultipartFile imageFile) {
        Post post = getPostById(id);
        String imageUrl = saveImage(imageFile);
        Member member = memberRepository.findById(memberId).orElseThrow(() -> new IllegalArgumentException("Member not found: " + memberId));
        post.setTitle(title);
        post.setDescription(description);
        post.setDeparture(departure);
        post.setDestination(destination);
        post.setDestinationLat(destinationLat);
        post.setDestinationLng(destinationLng);
        post.setWaypoint1(waypoint1);
        post.setWaypoint2(waypoint2);
        post.setWaypoint3(waypoint3);
        post.setThumbnail(imageUrl);
        post.setMember(member);
        return postRepository.save(post);
    }

    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }

    private String saveImage(MultipartFile imageFile) {
        if (imageFile.isEmpty()) {
            return null;
        }
        String fileName = UUID.randomUUID().toString() + "_" + StringUtils.cleanPath(imageFile.getOriginalFilename());
        Path uploadPath = Paths.get(fileDirPath + "/post/");

        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            Path filePath = uploadPath.resolve(fileName);
            Files.copy(imageFile.getInputStream(), filePath);
            return "/imagefile/post/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException("Could not save image file: " + fileName, e);
        }
    }
}