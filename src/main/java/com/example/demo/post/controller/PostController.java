package com.example.demo.post.controller;

import com.example.demo.member.entity.Member;
import com.example.demo.member.service.MemberService;
import com.example.demo.post.entity.Post;
import com.example.demo.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
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
                             @AuthenticationPrincipal UserDetails userDetails,
                             Model model) throws IOException {

        String author = userDetails.getUsername();
        Post post = postService.createPost(title, description, departure, departureLat, departureLng,
                destination, destinationLat, destinationLng,
                waypoints, waypointLats, waypointLngs, author, imageFile);
        return "redirect:/posts";
    }

    @GetMapping("/list")
    public String getAllPosts(Model model) {
        List<Post> posts = postService.getAllPosts();
        model.addAttribute("posts", posts);
        return "post/postList";
    }

    @GetMapping("/{id}")
    public String getPostById(@PathVariable Long id, Model model) {
        Post post = postService.getPostById(id);
        model.addAttribute("post", post);
        return "post/postDetail";
    }

    @PutMapping("/{id}")
    public String updatePost(@PathVariable Long id,
                             @RequestParam("title") String title,
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
                             @AuthenticationPrincipal UserDetails userDetails,
                             Model model) throws IOException {
        String author = userDetails.getUsername();
        Post post = postService.updatePost(id, title, description, departure, departureLat, departureLng,
                destination, destinationLat, destinationLng,
                waypoints, waypointLats, waypointLngs, author, imageFile);
        model.addAttribute("post", post);
        return "redirect:/posts";
    }
    @DeleteMapping("/{id}")
    public String deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return "redirect:/posts";
    }


    @GetMapping("/route")
    @ResponseBody
    public String getRoute(@RequestParam String origin,
                           @RequestParam String destination,
                           @RequestParam(required = false) String waypoints) {
        return postService.getRoute(origin, destination, waypoints);
    }
}