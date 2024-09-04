package com.example.demo.post.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class PostImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String filename;      // 이미지 파일 이름
    private String filepath;      // 이미지 파일 경로
    private String description;   // 이미지 설명

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;  // 각 이미지가 속한 Post 엔티티
}
