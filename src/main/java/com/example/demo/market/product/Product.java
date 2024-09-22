package com.example.demo.market.product;

import com.example.demo.global.base.BaseEntity;
import com.example.demo.member.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Product extends BaseEntity{

    private String name;
    private int price;
    private int categoryNumber;
    private String description; // description 필드 추가
    private String thumbnailFilepath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false) // 외래 키 컬럼을 명확히 설정
    private Member member;


    // Getter and Setter methods
}
