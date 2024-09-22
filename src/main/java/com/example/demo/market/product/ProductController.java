package com.example.demo.market.product;

import com.example.demo.member.entity.Member;
import com.example.demo.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
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
        // 로그인한 사용자 정보가 있으면 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated() && !(authentication instanceof AnonymousAuthenticationToken)) {
            // 사용자 정보 가져오기
            String username = authentication.getName();
            Optional<Member> memberOptional = memberService.findByUsername(username);  // 서비스에서 사용자 엔티티 조회
            if (memberOptional.isPresent()) {
                model.addAttribute("member", memberOptional.get());  // 사용자 엔티티를 모델에 전달
            } else {
                // 예외 처리 또는 기본값 설정
                model.addAttribute("member", new Member());  // 기본값 설정
            }
        }

        // 최신 등록된 상품 목록 가져오기
        List<Product> productList = productService.findLatestProducts();  // 서비스에서 최신 상품 리스트 조회
        if (productList == null) {
            productList = new ArrayList<>();  // productList가 null일 경우 빈 리스트로 초기화
        }
        model.addAttribute("latestProducts", productList);  // 상품 리스트를 모델에 전달

        return "market/market";  // Thymeleaf 템플릿 파일 이름
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
            @RequestParam("category") int categoryNumber,  // 카테고리 번호 입력받기
            @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) throws IOException {

        try {
            // 로그인한 사용자 정보로 Member 설정
            Member member = memberService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Member not found"));

            // 썸네일 및 이미지 처리 로직
            String thumbnailPath = null;
            if (thumbnail != null && !thumbnail.isEmpty()) {
                thumbnailPath = saveFile(thumbnail, fileDirPath);
            }

            // 상품 생성 및 저장
            Product product = new Product();
            product.setName(name);
            product.setPrice(price);
            product.setDescription(description);
            product.setCategoryNumber(categoryNumber);  // 카테고리 번호 저장
            product.setThumbnailFilepath(thumbnailPath);
            product.setMember(member);  // Member 객체 설정

            productService.saveProduct(product);

            return "redirect:/market/list"; // 상품 리스트로 리다이렉트

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "상품 등록 중 오류가 발생했습니다. 다시 시도해주세요.");
            return "market/createProduct"; // 오류 발생 시 등록 페이지로 이동
        }
    }


    private String saveFile(MultipartFile file, String uploadDir) throws IOException {
        // 원본 파일명과 확장자를 추출
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

        // UUID로 고유한 파일명 생성
        String uuid = UUID.randomUUID().toString();
        String savedFilename = uuid + extension;

        // 저장 경로 설정
        Path filePath = Paths.get(uploadDir, savedFilename);
        File dest = filePath.toFile();

        // 부모 디렉터리가 없을 경우 디렉터리 생성
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }

        // 파일을 지정된 경로에 저장
        file.transferTo(dest);

        // 저장된 파일명을 반환
        return savedFilename;
    }


}
