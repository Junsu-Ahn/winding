package com.example.market.service;

import com.example.demo.DemoApplication;
import com.example.demo.market.product.Product;
import com.example.demo.market.product.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = DemoApplication.class)
@ActiveProfiles("test")
public class ProductServiceTests {

    @Autowired
    private ProductService productService;

    @Test
    @DisplayName("마켓 제품 생성")
    void test1() {
        for(int i = 1; i <=200; i++) {
            String name = String.format("테스트 상품:[%03d]", i);
            int price = 1;
            productService.create(name, price);
        }
    }
}
