package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Integer price;

    @Column(name = "stock_quantity")  // ✅ 이것만 사용
    private Integer stockQuantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(length = 100)
    private String manufacturer;

    @Column(length = 100)
    private String modelNumber;

    @Column(length = 500)
    private String imageUrl;

    @Column(name = "is_available")
    @Builder.Default
    private Boolean isAvailable = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // === 비즈니스 로직 === //

    // 재고 추가
    public void addStock(Integer quantity) {
        this.stockQuantity += quantity;
    }

    // 재고 차감
    public void removeStock(Integer quantity) {
        if (this.stockQuantity < quantity) {
            throw new IllegalStateException("재고가 부족합니다. 현재 재고: " + this.stockQuantity);
        }
        this.stockQuantity -= quantity;
    }

    // 재고 설정
    public void setStock(Integer quantity) {
        this.stockQuantity = quantity;
    }

    // 상품 정보 업데이트
    public void updateProduct(String name, String description, Integer price,
                              Category category, String manufacturer, String modelNumber, String imageUrl) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.manufacturer = manufacturer;
        this.modelNumber = modelNumber;
        if (imageUrl != null) {
            this.imageUrl = imageUrl;
        }
        this.updatedAt = LocalDateTime.now();
    }

    // 재고 확인
    public boolean hasStock(Integer quantity) {
        return this.stockQuantity >= quantity;
    }

    // 품절 확인
    public boolean isOutOfStock() {
        return this.stockQuantity == null || this.stockQuantity == 0;
    }
    // 기본 정보 업데이트
    public void updateBasicInfo(String name, String description, Integer price, Category category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
    }

    // 재고 업데이트
    public void updateStock(Integer quantity) {
        this.stockQuantity = quantity;
    }

    // 이미지 URL 업데이트
    public void updateImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    // 제조사 정보 업데이트
    public void updateManufacturerInfo(String manufacturer, String modelNumber) {
        this.manufacturer = manufacturer;
        this.modelNumber = modelNumber;
    }

    // 판매 가능 여부 업데이트
    public void updateAvailability(Boolean isAvailable) {
        this.isAvailable = isAvailable;
    }
}