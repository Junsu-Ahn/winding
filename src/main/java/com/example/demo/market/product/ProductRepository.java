package com.example.demo.market.product;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findTop5ByOrderByCreateDateDesc();
    // 또는 페이징 처리가 필요한 경우
    List<Product> findTop5ByOrderByCreateDateDesc(Pageable pageable);
}
