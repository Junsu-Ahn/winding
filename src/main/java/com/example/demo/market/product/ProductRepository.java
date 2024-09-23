package com.example.demo.market.product;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {



    List<Product> findByCategoryNumber(Integer categoryNumber);

    // 추천 상품만 조회하는 메서드
    List<Product> findByIsRecommendedTrue();

    // 추천 상품을 제외한 최근 등록된 5개의 상품만 조회
    List<Product> findTop5ByIsRecommendedFalseOrderByCreateDateDesc();

    List<Product> findByNameContainingIgnoreCase(String keyword);
}
