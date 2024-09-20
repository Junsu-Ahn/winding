package com.example.demo.market.product;

import com.example.demo.global.base.BaseEntity;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Product extends BaseEntity {
    private String name;
    private int price;

}
