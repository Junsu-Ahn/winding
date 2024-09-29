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

    @Column(name = "description", length = 1000)  // 설명 필드에 제한 길이를 추가할 수 있음
    private String descriptions;  // 이미지 설명 (optional)

    private String filepath;  // 이미지 파일 경로

    @ManyToOne(fetch = FetchType.LAZY)  // Lazy 로딩 설정으로 필요할 때만 Post 로딩
    @JoinColumn(name = "post_id", nullable = false)  // post_id를 외래 키로 설정
    private Post post;  // 해당 이미지가 속한 게시글

    // Post와의 관계 설정 메서드 추가
    public void setPost(Post post) {
        this.post = post;
        if (!post.getImages().contains(this)) {  // Post의 이미지 리스트에 중복되지 않도록 추가
            post.getImages().add(this);
        }
    }
}