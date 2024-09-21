package com.example.demo.market.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/market")
@RequiredArgsConstructor
public class MarketController {

    @GetMapping("/main")
    public String mainPage(Model model) {
        // 로그인한 사용자 정보가 있으면 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            model.addAttribute("username", authentication.getName());  // 사용자 이름 전달
        }
        return "market/market";  // Thymeleaf 템플릿 파일 이름
    }
}
