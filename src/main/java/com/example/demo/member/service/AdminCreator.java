package com.example.demo.member.service;

import com.example.demo.member.repository.MemberRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class AdminCreator {

    private final MemberService memberService;
    private final MemberRepository memberRepository;

    public AdminCreator(MemberService memberService, MemberRepository memberRepository) {
        this.memberService = memberService;
        this.memberRepository = memberRepository;
    }

    @PostConstruct
    public void init() {
        if (!memberRepository.existsByUsername("admin")) {
            // admin 계정이 존재하지 않는 경우에만 생성
            memberService.createAdmin();
        }
    }
}
