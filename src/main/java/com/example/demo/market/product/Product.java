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
    private String description;
    private String thumbnailFilename;  // 썸네일 파일 이름 추가
    private String thumbnailFilepath;  // 썸네일 파일 경로

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();

    private boolean isRecommended = false;  // 기본값 false

    public void addImage(ProductImage image) {
        this.images.add(image);
        image.setProduct(this);  // 양방향 연관관계 설정
    }

    public void clearImages() {
        this.images.clear();
    }
}
