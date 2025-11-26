package com.example.demo.entity;

import com.example.demo.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name; // 카테고리 이름 (예: "센서", "모터", "보드", "케이블")

    @Column(length = 200)
    private String description; // 카테고리 설명

    // 계층 구조를 위한 parent_id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent; // 부모 카테고리

    @OneToMany(mappedBy = "parent")
    @Builder.Default
    private List<Category> children = new ArrayList<>(); // 자식 카테고리들

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Product> products = new ArrayList<>();

    // 비즈니스 메서드
    public void updateInfo(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public void setParent(Category parent) {
        this.parent = parent;
    }

    public void addChild(Category child) {
        this.children.add(child);
        child.setParent(this);
    }
}