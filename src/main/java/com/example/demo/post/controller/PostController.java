package com.example.demo.post.controller;

import com.example.demo.member.entity.Member;
import com.example.demo.member.service.MemberService;
import com.example.demo.post.entity.Post;
import com.example.demo.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final MemberService memberService;

    @Value("${custom.naver.api.client.id}")
    private String clientId;
    @Value("${custom.naver.api.client.secret}")
    private String clientSecret;

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
            @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail,  // 썸네일 이미지 (필수 아님)
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            @RequestParam(value = "imageDescriptions", required = false) List<String> imageDescriptions,  // 이미지 설명
            @RequestParam("memberId") Long memberId,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) throws IOException {

        try {
            // 게시글 생성 로직을 PostService에서 처리
            postService.createPost(title, description, departure, departureLat, departureLng, destination, destinationLat,
                    destinationLng, waypoints, waypointLats, waypointLngs, userDetails.getUsername(),
                    thumbnail, images, imageDescriptions, memberService.findById(memberId)
                            .orElseThrow(() -> new RuntimeException("Member not found")));

            // 게시물 리스트로 리다이렉트
            return "redirect:/posts/list";

        } catch (Exception e) {
            // 에러가 발생하면 콘솔에 에러 출력
            e.printStackTrace();

            // 에러 메시지를 모델에 추가하여 createPost 페이지에 전달
            model.addAttribute("errorMessage", "게시물 생성 중 오류가 발생했습니다. 다시 시도해주세요.");

            // 사용자가 입력한 값들을 다시 모델에 담아 반환
            model.addAttribute("title", title);
            model.addAttribute("description", description);
            model.addAttribute("departure", departure);
            model.addAttribute("departureLat", departureLat);
            model.addAttribute("departureLng", departureLng);
            model.addAttribute("destination", destination);
            model.addAttribute("destinationLat", destinationLat);
            model.addAttribute("destinationLng", destinationLng);
            model.addAttribute("waypoints", waypoints);
            model.addAttribute("waypointLats", waypointLats);
            model.addAttribute("waypointLngs", waypointLngs);

            // 썸네일 및 이미지 정보 복구
            model.addAttribute("thumbnail", thumbnail);
            model.addAttribute("images", images);
            model.addAttribute("imageDescriptions", imageDescriptions);

            // 이미지 설명도 오류 발생 시 복구하여 사용자에게 표시
            if (imageDescriptions != null && !imageDescriptions.isEmpty()) {
                for (int i = 0; i < imageDescriptions.size(); i++) {
                    model.addAttribute("imageDescription" + i, imageDescriptions.get(i));
                }
            }

            return "post/createPost"; // 오류 발생 시 createPost 페이지로 다시 이동
        }
    }


    @GetMapping("/list")
    public String getAllPosts(Model model) {
        List<Post> posts = postService.getAllPosts(); // 모든 게시글 조회
        model.addAttribute("posts", posts);
        model.addAttribute("regionName", "전체");  // 전체 목록에 대한 제목 설정
        return "post/postList";  // 동일한 템플릿 사용
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
        model.addAttribute("naverClientId", clientId);

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

    @GetMapping("/region/{regionCode}")
    public String getPostsByRegion(@PathVariable int regionCode, Model model) {
        List<Post> posts = postService.getPostsByRegionCode(regionCode);

        // 지역명 설정
        String regionName = getRegionName(regionCode);

        model.addAttribute("posts", posts);
        model.addAttribute("regionName", regionName); // 지역명 전달
        return "post/postList";  // 템플릿 이름
    }

    private String getRegionName(int regionCode) {
        switch (regionCode) {
            case 1: return "경기도";
            case 2: return "강원도";
            case 3: return "경상북도";
            case 4: return "경상남도";
            case 5: return "전라남도";
            case 6: return "전라북도";
            case 7: return "충청북도";
            case 8: return "충청남도";
            default: return "기타 지역";
        }
    }

}