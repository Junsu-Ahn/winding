package com.example.demo.market.product;

import com.example.demo.member.entity.Member;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {
    private final ProductRepository productRepository;

    @Value("${custom.fileDirPath}")
    private String fileDirPath;

    public List<Product> getList() {
        return productRepository.findAll();
    }

    public void createProduct(String name, List<String> descriptions, int price, int categoryNumber,
                              MultipartFile thumbnail, List<MultipartFile> imageFiles,
                              Member member, boolean isRecommended) throws IOException {

        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setDescriptions(descriptions);
        product.setCategoryNumber(categoryNumber);
        product.setMember(member);
        product.setRecommended(isRecommended);

        // 썸네일 저장 처리
        if (thumbnail != null && !thumbnail.isEmpty()) {
            String originalThumbnailName = thumbnail.getOriginalFilename();
            String savedThumbnail = saveImageFile(thumbnail);
            product.setThumbnailFilename(originalThumbnailName);
            product.setThumbnailFilepath("/imagefile/post/" + savedThumbnail);
        }

        // 추가 이미지 파일 처리 (List로 처리)
        if (imageFiles != null && !imageFiles.isEmpty()) {
            System.out.println("Number of image files: " + imageFiles.size());
            for (MultipartFile imageFile : imageFiles) {
                if (!imageFile.isEmpty()) {
                    String savedFilename = saveImageFile(imageFile);
                    System.out.println("Saved Filename: " + savedFilename);
                    product.getImages().add("/imagefile/post/" + savedFilename);
                } else {
                    System.out.println("Empty image file detected");
                }
            }
        } else {
            System.out.println("No image files to process.");
        }

        // 디버깅용 로그
        System.out.println("Product Images Before Save: " + product.getImages());

        productRepository.save(product);
    }




    // 이미지 파일을 저장하는 유틸리티 함수
    private String saveImageFile(MultipartFile file) throws IOException {
        String uuid = UUID.randomUUID().toString();
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String savedFilename = uuid + extension;

        String filePath = Paths.get(fileDirPath, savedFilename).toString();
        File dest = new File(filePath);

        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }
        System.out.println("파일 저장 경로: " + filePath);
        System.out.println("원본 파일명: " + originalFilename);
        System.out.println("저장된 파일명: " + savedFilename);
        file.transferTo(dest);
        return savedFilename;
    }


    public List<Product> getProductsByCategory(Integer categoryNumber) {
        return productRepository.findByCategoryNumber(categoryNumber);
    }

    public String getCategoryName(int categoryNumber) {
        switch (categoryNumber) {
            case 1:
                return "세차용품";
            case 2:
                return "엔진오일";
            case 3:
                return "워셔액";
            case 4:
                return "차량 관리용품";
            case 5:
                return "액세서리";
            default:
                return "기타";
        }
    }

    public void displayProductDetails(Product product, Model model) {
        String categoryName = getCategoryName(product.getCategoryNumber());
        model.addAttribute("categoryName", categoryName);
        model.addAttribute("product", product);

    }

    public void saveProduct(Product product) {
        productRepository.save(product);
    }

    // 일반 상품만 조회 (추천 상품 제외)
    public List<Product> findLatestProducts() {
        return productRepository.findTop5ByIsRecommendedFalseOrderByCreateDateDesc();
    }

    // 추천 상품만 조회
    public List<Product> findRecommendedProducts() {
        return productRepository.findByIsRecommendedTrue();
    }

    public List<Product> searchProductsByName(String keyword) {
        return productRepository.findByNameContainingIgnoreCase(keyword);
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id).orElse(null);
    }
}
