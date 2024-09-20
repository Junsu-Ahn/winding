package com.example.demo.market.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/market")
@RequiredArgsConstructor
public class MarketController {

    @GetMapping("/main")
    public String index() {
        return "market/market";
    }
}
