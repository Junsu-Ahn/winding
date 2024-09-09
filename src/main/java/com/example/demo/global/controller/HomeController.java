package com.example.demo.global.controller;

import com.example.demo.post.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final PostService postService;

    @GetMapping("/")
    public String mainPage(Model model) {
        // 인기 드라이브 코스 (조회수 순)
        model.addAttribute("popularDrives", postService.getPopularPosts());

        // 최신 드라이브 코스 (등록일 순)
        model.addAttribute("latestDrives", postService.getLatestPosts());

        return "home/main2";  // mainPage.html로 이동
    }

    @GetMapping("/map")
    public String home2() {
        return "home/main";
    }
}
