package com.example.demo.global.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.ui.Model;
import com.example.demo.member.entity.Member;
import com.example.demo.member.repository.MemberRepository;
import com.example.demo.post.entity.Post;
import com.example.demo.post.repository.PostRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class AdminController {

    private final MemberRepository memberRepository;
    private final PostRepository postRepository;

    public AdminController(MemberRepository memberRepository, PostRepository postRepository) {
        this.memberRepository = memberRepository;
        this.postRepository = postRepository;
    }

    @GetMapping("/admin")
    public String adminPage(Model model,
                            @RequestParam(name = "page", defaultValue = "0") int page,
                            @RequestParam(name = "size", defaultValue = "5") int size) {
        Page<Member> memberPage = memberRepository.findAll(PageRequest.of(page, size));
        Page<Post> postPage = postRepository.findAll(PageRequest.of(page, size));

        model.addAttribute("members", memberPage.getContent());
        model.addAttribute("posts", postPage.getContent());
        model.addAttribute("memberPage", memberPage);
        model.addAttribute("postPage", postPage);

        return "admin/admin";
    }


    @GetMapping("/admin/search")
    public String search(@RequestParam(name = "query") String query,
                         @RequestParam(name = "page", defaultValue = "0") int page,
                         @RequestParam(name = "size", defaultValue = "5") int size,
                         Model model) {
        Page<Member> memberPage = memberRepository.findByUsernameContaining(query, PageRequest.of(page, size));
        Page<Post> postPage = postRepository.findByTitleContaining(query, PageRequest.of(page, size));

        model.addAttribute("members", memberPage.getContent());
        model.addAttribute("posts", postPage.getContent());
        model.addAttribute("memberPage", memberPage);
        model.addAttribute("postPage", postPage);
        model.addAttribute("query", query);

        if (memberPage.isEmpty() && postPage.isEmpty()) {
            model.addAttribute("message", "검색결과 없음");
        }

        return "admin/searchResults";
    }


    @PostMapping("/admin/members/delete")
    public String deleteMembers(@RequestParam(name = "memberIds") List<Long> memberIds) {
        if (memberIds != null && !memberIds.isEmpty()) {
            memberRepository.deleteAllById(memberIds);
        }
        return "redirect:/admin";
    }

    @PostMapping("/admin/posts/delete")
    public String deletePosts(@RequestParam(name = "postIds") List<Long> postIds) {
        if (postIds != null && !postIds.isEmpty()) {
            postRepository.deleteAllById(postIds);
        }
        return "redirect:/admin";
    }
}