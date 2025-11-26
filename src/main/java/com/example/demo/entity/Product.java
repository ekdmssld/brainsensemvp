package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name; // 상품명

    @Column(nullable = false)
    private Integer price; // 가격

    @Column(columnDefinition = "TEXT")
    private String description; // 상품 설명

    @Column(length = 500)
    private String imageUrl; // 이미지 URL

    @Column(nullable = false)
    @Builder.Default
    private Integer stock = 0; // 재고 (stock으로 필드명 변경)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category; // 카테고리

    @Column(length = 100)
    private String manufacturer; // 제조사

    @Column(length = 100)
    private String modelNumber; // 모델 번호

    @Column
    @Builder.Default
    private Boolean isAvailable = true; // 판매 가능 여부

    @Column(updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now(); // 등록일

    @Column
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now(); // 수정일

    // 비즈니스 메서드
    public void updateInfo(String name, String description, Integer price, String imageUrl,
                           String manufacturer, String modelNumber) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.manufacturer = manufacturer;
        this.modelNumber = modelNumber;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateCategory(Category category) {
        this.category = category;
        this.updatedAt = LocalDateTime.now();
    }

    public void addStock(int quantity) {
        this.stock += quantity;
        this.updatedAt = LocalDateTime.now();
    }

    public void removeStock(int quantity) {
        int restStock = this.stock - quantity;
        if (restStock < 0) {
            throw new IllegalStateException("재고가 부족합니다.");
        }
        this.stock = restStock;
        this.updatedAt = LocalDateTime.now();
    }

    public void changeAvailability(Boolean isAvailable) {
        this.isAvailable = isAvailable;
        this.updatedAt = LocalDateTime.now();
    }

    // Getter 추가 (stock)
    public Integer getStockQuantity() {
        return this.stock;
    }
}