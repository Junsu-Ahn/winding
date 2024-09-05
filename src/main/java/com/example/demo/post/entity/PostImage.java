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

    private String filename;  // 저장된 이미지 파일명
    private String descriptions;  // 이미지 설명 (optional)
    private String filepath;

    @ManyToOne
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;  // 해당 이미지가 속한 게시글
}
