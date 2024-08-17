package com.example.demo.post.controller;

import com.example.demo.member.entity.Member;
import com.example.demo.member.service.MemberService;
import com.example.demo.post.entity.Post;
import com.example.demo.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final MemberService memberService;

    @GetMapping("/create")
    public String showCreateForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/member/login"; // 로그인 페이지로 리다이렉트
        }

        Optional<Member> optionalMember = memberService.findByUsername(userDetails.getUsername());

        if (optionalMember.isPresent()) {
            Member member = optionalMember.get();
            model.addAttribute("author", userDetails.getUsername());
            model.addAttribute("userAddress", member.getAddress()); // 사용자의 주소를 모델에 추가
        } else {
            model.addAttribute("author", userDetails.getUsername());
            model.addAttribute("userAddress", "주소 정보 없음"); // 기본값 설정 등
        }

        return "post/createPost";
    }

    @PostMapping("/create")
    public String createPost(@RequestParam("title") String title,
                             @RequestParam("description") String description,
                             @RequestParam("departure") String departure,
                             @RequestParam("departureLat") double departureLat,
                             @RequestParam("departureLng") double departureLng,
                             @RequestParam("destination") String destination,
                             @RequestParam("destinationLat") double destinationLat,
                             @RequestParam("destinationLng") double destinationLng,
                             @RequestParam(value = "waypoints", required = false) List<String> waypoints,
                             @RequestParam(value = "waypointLats", required = false) List<Double> waypointLats,
                             @RequestParam(value = "waypointLngs", required = false) List<Double> waypointLngs,
                             @RequestParam("image") MultipartFile imageFile,
                             @AuthenticationPrincipal UserDetails userDetails) throws IOException {

        if (waypoints == null) {
            waypoints = List.of();
        }
        if (waypointLats == null) {
            waypointLats = List.of();
        }
        if (waypointLngs == null) {
            waypointLngs = List.of();
        }

        String author = userDetails.getUsername();
        postService.createPost(title, description, departure, departureLat, departureLng,
                destination, destinationLat, destinationLng, waypoints, waypointLats, waypointLngs,
                author, imageFile);

        return "post/postList";
    }


    @GetMapping("/list")
    public String list(Model model) {
        List<Post> posts = this.postService.getAllPosts();

        model.addAttribute("posts", posts);

        return "post/postList";
    }


    // 게시글 상세 보기
    @GetMapping("/detail/{id}")
    public String getPostById(@PathVariable("id") Long id, Model model) {
        Post post = postService.getPostById(id);

        if (post == null) {
            return "redirect:/posts/list"; // 포스트가 없는 경우 목록으로 리다이렉트
        }
        String formattedCreateDate = post.getCreateDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        model.addAttribute("post", post);
        model.addAttribute("formattedCreateDate", formattedCreateDate);

        if (post.getModifyDate() != null) {
            model.addAttribute("formattedModifyDate", post.getModifyDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
        }
        return "post/postDetail";
    }



    @DeleteMapping("/detail/{id}")
    public String deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return "redirect:/posts/list";  // 경로 수정
    }



    @GetMapping("/route")
    @ResponseBody
    public String getRoute(@RequestParam String origin,
                           @RequestParam String destination,
                           @RequestParam(required = false) String waypoints) {
        return postService.getRoute(origin, destination, waypoints);
    }
}