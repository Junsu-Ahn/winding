package com.example.demo.market.product;

import com.example.demo.member.entity.Member;
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
public class ProductService {
    private final ProductRepository productRepository;

    @Value("${custom.fileDirPath}")
    private String fileDirPath;

    public List<Product> getList() {
        return productRepository.findAll();
    }

    public void createProduct(String name, String description, int price, int categoryNumber,
                              MultipartFile thumbnail, List<MultipartFile> imageFiles,
                              Member member) throws IOException {

        // Product 객체 생성
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setCategoryNumber(categoryNumber);
        product.setMember(member);

        // 썸네일 이미지 처리 (무조건 입력)
        if (thumbnail != null && !thumbnail.isEmpty()) {
            String originalThumbnailName = thumbnail.getOriginalFilename();  // 원본 파일명
            String savedThumbnail = saveImageFile(thumbnail);  // 파일 저장
            product.setThumbnailFilename(originalThumbnailName);  // 원본 파일명 저장
            product.setThumbnailFilepath("/imagefile/post/" + savedThumbnail);  // 파일 경로 저장
        }

        // 추가 이미지 파일 처리
        if (imageFiles != null && !imageFiles.isEmpty()) {
            for (MultipartFile imageFile : imageFiles) {
                if (!imageFile.isEmpty()) {
                    String originalFilename = imageFile.getOriginalFilename();
                    String savedFilename = saveImageFile(imageFile);

                    // 이미지 파일 처리 로직 추가
                    ProductImage productImage = new ProductImage();
                    productImage.setFilename(originalFilename);  // 원본 파일명 저장
                    productImage.setFilepath("/imagefile/product/" + savedFilename);

                    // Product에 이미지 추가
                    product.addImage(productImage);
                }
            }
        }

        // Product 저장
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

        // 디버깅 로그 추가
        System.out.println("파일 저장 경로: " + filePath);

        // 파일 경로가 존재하지 않으면 디렉토리 생성
        if (!dest.getParentFile().exists()) {
            boolean created = dest.getParentFile().mkdirs();
            if (!created) {
                System.err.println("디렉토리 생성 실패: " + dest.getParentFile().getPath());
                return null;
            }
        }

        // 파일 저장
        file.transferTo(dest);
        return savedFilename;
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

    public List<Product> findLatestProducts() {
        Pageable pageable = PageRequest.of(0, 5);  // 첫 페이지, 5개의 항목
        return productRepository.findTop5ByOrderByCreateDateDesc(pageable);
    }

    public List<Product> getProductsByCategoryNumber(int categoryNumber) {
        return productRepository.findByCategoryNumber(categoryNumber);
    }

}
