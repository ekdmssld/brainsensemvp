package com.example.demo.entity;

import com.example.demo.entity.Product;
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

    // 비즈니스 메서드
    public void updateQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void addQuantity(int quantity) {
        this.quantity += quantity;
    }

    public int getTotalPrice() {
        return this.product.getPrice() * this.quantity;
    }

    public static Cart createCart(User member, Product product, int quantity) {
        return Cart.builder()
                .member(member)
                .product(product)
                .quantity(quantity)
                .build();
    }
}