package com.example.demo.market.product;

import com.example.demo.member.entity.Member;
import com.example.demo.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
@RequestMapping("/market")
public class ProductController {

    private final ProductService productService;
    private final MemberService memberService;

    @Value("${custom.fileDirPath}")
    private String fileDirPath;

    @GetMapping("/main")
    public String mainPage(Model model) {
        // 최근 등록된 일반 상품만 조회
        List<Product> latestProducts = productService.findLatestProducts();

        // 추천 상품만 조회
        List<Product> recommendedProducts = productService.findRecommendedProducts();

        // 모델에 추가
        model.addAttribute("latestProducts", latestProducts);
        model.addAttribute("recommendedProducts", recommendedProducts);

        return "market/market";
    }

    @GetMapping("/list")
    public String list(Model model) {
        List<Product> productList = productService.getList();
        model.addAttribute("productList", productList);
        return "market/list";
    }

    @GetMapping("/detail/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {

        return "market/detail";
    }

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
            return "redirect:/error"; // 오류 페이지로 리다이렉트
        }

        return "market/createProduct"; // 상품 등록 페이지로 이동
    }

    @PostMapping("/create")
    public String createProduct(
            @RequestParam("name") String name,
            @RequestParam("price") int price,
            @RequestParam("description") String description,
            @RequestParam("category") int categoryNumber,
            @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,  // 이미지 리스트 추가
            @RequestParam(value = "isRecommended", required = false) boolean isRecommended,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) throws IOException {

        try {
            // 로그인한 사용자 정보로 Member 설정
            Member member = memberService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Member not found"));

            // 관리자인 경우에만 추천 상품으로 설정 가능
            if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
                isRecommended = false;  // 일반 사용자는 추천 상품 등록 불가
            }

            // 상품 등록 처리
            productService.createProduct(name, description, price, categoryNumber, thumbnail, images, member, isRecommended);

            return "redirect:/market/main";  // 상품 리스트로 리다이렉트

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "상품 등록 중 오류가 발생했습니다. 다시 시도해주세요.");
            return "market/createProduct";
        }
    }


    @GetMapping("/category/{categoryNumber}")
    public String getCategoryProducts(@PathVariable("categoryNumber") Integer categoryNumber, Model model) {
        // 서비스에서 카테고리별 상품 리스트 가져오기
        List<Product> productList = productService.getProductsByCategory(categoryNumber);

        // 상품이 없으면 빈 리스트를 초기화하여 NullPointerException 방지
        if (productList == null) {
            productList = new ArrayList<>();
        }

        // 모델에 카테고리 번호와 상품 리스트 추가
        model.addAttribute("productList", productList);
        model.addAttribute("categoryNumber", categoryNumber);

        return "market/list"; // list.html로 이동
    }

    @GetMapping("/search")
    public String searchProducts(@RequestParam("keyword") String keyword, Model model) {
        List<Product> productList = productService.searchProductsByName(keyword);
        model.addAttribute("productList", productList);
        model.addAttribute("keyword", keyword); // 검색어를 다시 전달해 UI에 표시
        return "market/list"; // 검색 결과를 list.html에서 출력
    }

}
