package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "carts")
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private User member; // 사용자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product; // 상품

    @Column(nullable = false)
    private Integer quantity; // 수량

    @Column(updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // 수량 변경
    public void changeQuantity(Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("수량은 1개 이상이어야 합니다.");
        }
        if (quantity > product.getStockQuantity()) {
            throw new IllegalArgumentException("재고가 부족합니다.");
        }
        this.quantity = quantity;
    }

    // 수량 증가
    public void addQuantity(Integer amount) {
        this.quantity += amount;
        if (this.quantity > product.getStockQuantity()) {
            throw new IllegalArgumentException("재고가 부족합니다.");
        }
    }

    // 총 금액 계산
    public Integer getTotalPrice() {
        return this.product.getPrice() * this.quantity;
    }

    // 정적 팩토리 메서드
    public static Cart createCart(User member, Product product, Integer quantity) {
        if (quantity > product.getStockQuantity()) {
            throw new IllegalArgumentException("재고가 부족합니다.");
        }

        return Cart.builder()
                .member(member)
                .product(product)
                .quantity(quantity)
                .createdAt(LocalDateTime.now())
                .build();
    }
}