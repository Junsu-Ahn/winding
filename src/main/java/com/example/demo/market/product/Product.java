package com.example.demo.market.product;

import com.example.demo.global.base.BaseEntity;
import com.example.demo.member.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Product extends BaseEntity {

    private String name;
    private int price;
    private int categoryNumber;

    @ElementCollection // 별도의 테이블을 생성하여 리스트를 관리
    private List<String> descriptions = new ArrayList<>(); // 설명을 리스트로 관리

    private String thumbnailFilename;  // 썸네일 파일 이름 추가
    private String thumbnailFilepath;  // 썸네일 파일 경로

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)  // ProductImage와 일대다 관계 설정
    private List<ProductImage> images = new ArrayList<>();  // 추가 이미지 리스트


    private boolean isRecommended = false;  // 기본값 false

    // 이미지 추가 메서드
    public void addImage(ProductImage image) {
        images.add(image);
        image.setProduct(this);  // 연관 관계 설정
    }

    // 이미지 삭제 메서드
    public void removeImage(ProductImage image) {
        images.remove(image);
        image.setProduct(null);
    }

}
