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

    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "images")
    private List<String> images = new ArrayList<>();  // 추가 이미지 경로 리스트

    @ElementCollection
    @CollectionTable(name = "product_descriptions", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "descriptions")
    private List<String> descriptions = new ArrayList<>();  // 설명 리스트

    private String thumbnailFilename;  // 썸네일 파일 이름 추가
    private String thumbnailFilepath;  // 썸네일 파일 경로

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;


    private boolean isRecommended = false;  // 기본값 false

}
