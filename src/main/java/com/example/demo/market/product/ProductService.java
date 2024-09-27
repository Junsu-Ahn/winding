package com.example.demo.market.product;

import com.example.demo.member.entity.Member;
import com.example.demo.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final MemberRepository memberRepository;

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

        if (thumbnail != null && !thumbnail.isEmpty()) {
            String savedThumbnail = saveImageFile(thumbnail);
            product.setThumbnailFilepath("/imagefile/post/" + savedThumbnail);
        }

        if (imageFiles != null && !imageFiles.isEmpty()) {
            for (MultipartFile imageFile : imageFiles) {
                if (!imageFile.isEmpty()) {
                    String savedFilename = saveImageFile(imageFile);
                    product.getImages().add("/imagefile/post/" + savedFilename);
                }
            }
        }

        productRepository.save(product);
    }

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

        file.transferTo(dest);
        return savedFilename;
    }

    public List<Product> getProductsByCategory(Integer categoryNumber) {
        return productRepository.findByCategoryNumber(categoryNumber);
    }

    public void displayProductDetails(Product product, Model model) {
        String categoryName = getCategoryName(product.getCategoryNumber());
        model.addAttribute("categoryName", categoryName);
        model.addAttribute("product", product);
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


    public List<Product> findLatestProducts() {
        return productRepository.findTop5ByIsRecommendedFalseOrderByCreateDateDesc();
    }

    public List<Product> findRecommendedProducts() {
        return productRepository.findByIsRecommendedTrue();
    }

    public List<Product> searchProductsByName(String keyword) {
        return productRepository.findByNameContainingIgnoreCase(keyword);
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public Optional<Product> findById(Long productId) {
        return productRepository.findById(productId);
    }
}
