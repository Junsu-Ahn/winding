package com.example.demo.post.controller;

import com.example.demo.post.entity.Post;
import com.example.demo.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping("/create")
    public String showCreateForm() {
        return "post/createPost";
    }

    @PostMapping("/create")
    public String createPost(@RequestParam("title") String title,
                             @RequestParam("description") String description,
                             @RequestParam("departure") String departure,
                             @RequestParam("destination") String destination,
                             @RequestParam("destinationLat") double destinationLat,
                             @RequestParam("destinationLng") double destinationLng,
                             @RequestParam(value = "waypoint1", required = false) String waypoint1,
                             @RequestParam(value = "waypoint2", required = false) String waypoint2,
                             @RequestParam(value = "waypoint3", required = false) String waypoint3,
                             @RequestParam("memberId") Long memberId,
                             @RequestParam("image") MultipartFile imageFile,
                             Model model) throws IOException {
        Post post = postService.createPost(title, description, departure, destination, destinationLat, destinationLng, waypoint1, waypoint2, waypoint3, memberId, imageFile);
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
                             @RequestParam("destination") String destination,
                             @RequestParam("destinationLat") double destinationLat,
                             @RequestParam("destinationLng") double destinationLng,
                             @RequestParam(value = "waypoint1", required = false) String waypoint1,
                             @RequestParam(value = "waypoint2", required = false) String waypoint2,
                             @RequestParam(value = "waypoint3", required = false) String waypoint3,
                             @RequestParam("memberId") Long memberId,
                             @RequestParam("image") MultipartFile imageFile,
                             Model model) throws IOException {
        Post post = postService.updatePost(id, title, description, departure, destination, destinationLat, destinationLng, waypoint1, waypoint2, waypoint3, memberId, imageFile);
        model.addAttribute("post", post);
        return "redirect:/posts";
    }

    @DeleteMapping("/{id}")
    public String deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return "redirect:/posts";
    }
}