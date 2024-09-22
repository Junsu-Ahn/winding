package com.example.demo.market.product;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public List<Product> getList() {
        return productRepository.findAll();
    }

    public void create(String name, int price) {
        Product p = new Product();
        p.setName(name);
        p.setPrice(price);
        productRepository.save(p);
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
}
