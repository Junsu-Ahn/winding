package com.example.demo.post.controller;

import com.example.demo.member.entity.Member;
import com.example.demo.member.service.MemberService;
import com.example.demo.post.entity.Post;
import com.example.demo.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final MemberService memberService;

    @Value("${custom.naver.api.client.id}")
    private String naverClientId;

    @Value("${custom.fileDirPath}")
    private String fileDirPath;

    @GetMapping("/create")
    public String showCreateForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/member/login"; // 로그인 페이지로 리다이렉트
        }

        Optional<Member> optionalMember = memberService.findByUsername(userDetails.getUsername());

        if (optionalMember.isPresent()) {
            Member member = optionalMember.get();
            model.addAttribute("member", member); // Member 객체를 모델에 추가
        } else {
            return "redirect:/error"; // 적절한 오류 페이지로 리다이렉트
        }
        return "post/createPost";
    }

    @PostMapping("/create")
    public String createPost(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("departure") String departure,
            @RequestParam(value = "departureLat", required = false) Double departureLat,
            @RequestParam(value = "departureLng", required = false) Double departureLng,
            @RequestParam("destination") String destination,
            @RequestParam(value = "destinationLat", required = false) Double destinationLat,
            @RequestParam(value = "destinationLng", required = false) Double destinationLng,
            @RequestParam(value = "waypoints", required = false) List<String> waypoints,
            @RequestParam(value = "waypointLats", required = false) List<Double> waypointLats,
            @RequestParam(value = "waypointLngs", required = false) List<Double> waypointLngs,
            @RequestParam("thumbnail") MultipartFile thumbnail,  // 썸네일 이미지
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            @RequestParam(value = "imageDescriptions", required = false) List<String> imageDescriptions,  // 이미지 설명
            @RequestParam("memberId") Long memberId,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) throws IOException {

        // 썸네일과 다른 이미지 처리 로직은 PostService에서 처리
        postService.createPost(title, description, departure, departureLat, departureLng, destination, destinationLat,
                destinationLng, waypoints, waypointLats, waypointLngs, userDetails.getUsername(),
                thumbnail, images, imageDescriptions, memberService.findById(memberId)
                        .orElseThrow(() -> new RuntimeException("Member not found")));

        return "redirect:/posts/list";  // 게시물 리스트로 리다이렉트
    }

    @GetMapping("/list")
    public String list(Model model) {
        List<Post> posts = this.postService.getAllPosts();

        model.addAttribute("posts", posts);
        return "post/postList";
    }


    // 게시글 상세 보기
    @GetMapping("/detail/{id}")
    public String getPostById(@AuthenticationPrincipal UserDetails userDetails, @PathVariable("id") Long id, Model model) {

        if (userDetails == null) {
            return "redirect:/member/login"; // 로그인 페이지로 리다이렉트
        }

        Post post = postService.getPostById(id);

        if (post == null) {
            return "redirect:/posts/list"; // 포스트가 없는 경우 목록으로 리다이렉트
        }

        // 게시물 작성 날짜를 포맷팅
        String formattedCreateDate = post.getCreateDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        model.addAttribute("post", post);
        model.addAttribute("formattedCreateDate", formattedCreateDate);

        postService.incrementViewCount(id);

        // Naver Client ID 추가
        model.addAttribute("naverClientId", naverClientId);

        // 수정 날짜가 있는 경우 포맷팅하여 모델에 추가
        if (post.getModifyDate() != null) {
            model.addAttribute("formattedModifyDate", post.getModifyDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        }

        return "post/postDetail";
    }

    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostData(@PathVariable("id") Long id) {
        Post post = postService.getPostById(id);
        if (post != null) {
            return ResponseEntity.ok(post);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/detail/{id}")
    public String deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return "redirect:/posts/list";  // 경로 수정
    }

    @GetMapping("/test")
    public String test()
    {
        return "post/test";
    }

    @GetMapping("/search")
    public String listPosts(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        List<Post> posts;
        if (keyword != null && !keyword.isEmpty()) {
            posts = postService.searchPostsByKeyword(keyword);
        } else {
            posts = postService.getAllPosts();
        }
        model.addAttribute("posts", posts);
        model.addAttribute("keyword", keyword); // 검색어도 전달
        return "post/postList"; // 동일한 템플릿 사용
    }

}