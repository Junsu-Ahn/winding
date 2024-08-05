package com.example.demo.post.dto;

import com.example.demo.global.base.BaseEntity;
import jakarta.persistence.Entity;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class PostForm extends BaseEntity {
    private String title;
    private String description;
    private String location;
    private String mapUrl;
    private String author;
    private String imageUrl;  // 이미지 URL
}
