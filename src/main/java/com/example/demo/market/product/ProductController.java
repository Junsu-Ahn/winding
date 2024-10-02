package com.example.demo.market.product;

import com.example.demo.member.entity.Member;
import com.example.demo.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

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
        List<Product> latestProducts = productService.findLatestProducts();
        List<Product> recommendedProducts = productService.findRecommendedProducts();

        Map<Long, String> formattedPrices = new HashMap<>();
        DecimalFormat formatter = new DecimalFormat("###,###");

        for (Product product : latestProducts) {
            String formattedPrice = formatter.format(product.getPrice());
            formattedPrices.put(product.getId(), formattedPrice);
        }

        for (Product product : recommendedProducts) {
            String formattedPrice = formatter.format(product.getPrice());
            formattedPrices.put(product.getId(), formattedPrice);
        }

        model.addAttribute("latestProducts", latestProducts);
        model.addAttribute("recommendedProducts", recommendedProducts);
        model.addAttribute("formattedPrices", formattedPrices);

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
        Product product = productService.getProductById(id).orElse(null);
        if (product == null) {
            return "redirect:/market/list";
        }

        productService.displayProductDetails(product, model);
        return "market/detail";
    }

    @GetMapping("/create")
    public String showCreateForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/member/login";
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(role -> role.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin);  // 관리자 여부를 Model에 추가
        Optional<Member> optionalMember = memberService.findByUsername(userDetails.getUsername());

        if (optionalMember.isPresent()) {
            Member member = optionalMember.get();
            model.addAttribute("member", member);
        } else {
            return "redirect:/error";
        }

        return "market/createProduct";
    }

    @PostMapping("/create")
    public String createProduct(
            @RequestParam("name") String name,
            @RequestParam("price") int price,
            @RequestParam(value = "descriptions", required = false) List<String> descriptions,
            @RequestParam("category") int categoryNumber,
            @RequestParam(value = "thumbnail", required = false) MultipartFile thumbnail,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            @RequestParam(value = "isRecommended", required = false) boolean isRecommended,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) throws IOException {

        try {
            Member member = memberService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Member not found"));

            if (!userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
                isRecommended = false;
            }

            productService.createProduct(name, descriptions, price, categoryNumber, thumbnail, images, member, isRecommended);
            return "redirect:/market/main";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "상품 등록 중 오류가 발생했습니다. 다시 시도해주세요.");
            return "market/createProduct";
        }
    }

    @GetMapping("/category/{categoryNumber}")
    public String getCategoryProducts(@PathVariable("categoryNumber") Integer categoryNumber, Model model) {
        List<Product> productList = productService.getProductsByCategory(categoryNumber);

        if (productList == null) {
            productList = new ArrayList<>();
        }

        model.addAttribute("productList", productList);
        model.addAttribute("categoryNumber", categoryNumber);

        return "market/list";
    }

    @GetMapping("/search")
    public String searchProducts(@RequestParam("keyword") String keyword, Model model) {
        List<Product> productList = productService.searchProductsByName(keyword);
        model.addAttribute("productList", productList);
        model.addAttribute("keyword", keyword);
        return "market/list";
    }

    @PostMapping("/wishlist/add/{productId}")
    public ResponseEntity<String> addToWishlist(
            @PathVariable("productId") Long productId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Member member = memberService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Member not found"));
            Product product = productService.findById(productId).orElseThrow(() -> new RuntimeException("Product not found"));

            memberService.addToWishlist(member, product);
            return ResponseEntity.ok("Product added to wishlist");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error adding product to wishlist");
        }
    }

    @PostMapping("/cart/add/{productId}")
    public ResponseEntity<String> addToCart(
            @PathVariable("productId") Long productId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Member member = memberService.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("Member not found"));
            Product product = productService.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found"));

            memberService.addToCart(member, product);
            return ResponseEntity.ok("Product added to cart");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error adding product to cart");
        }
    }
}
