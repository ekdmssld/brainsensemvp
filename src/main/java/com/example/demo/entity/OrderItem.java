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
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order; // 주문

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private User member; // 주문자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product; // 상품

    @Column(nullable = false)
    private Integer quantity; // 수량

    @Column(nullable = false)
    private Integer price; // 주문 가격 (주문 당시의 상품 가격)

    @Column(updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now(); // 생성일

    // 비즈니스 메서드
    public void setOrder(Order order) {
        this.order = order;
    }

    public int getTotalPrice() {
        return this.price * this.quantity;
    }

    public static OrderItem createOrderItem(User member, Product product, int quantity) {
        // 재고 확인
        if (product.getStockQuantity() < quantity) {
            throw new IllegalStateException("재고가 부족합니다. 상품: " + product.getName());
        }

        // 재고 차감
        product.removeStock(quantity);

        return OrderItem.builder()
                .member(member)
                .product(product)
                .price(product.getPrice())
                .quantity(quantity)
                .build();
    }

    public void cancel() {
        // 재고 복구
        this.product.addStock(this.quantity);
    }
}