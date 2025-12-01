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
public class OrderItem {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private Integer quantity;     // 수량
    private Integer price;        // 단가
    private Integer totalPrice;   // price * quantity

    // ---------------------
    // ★ 주문 아이템 생성 메서드 (프로젝트 전체에서 이것만 사용)
    // ---------------------
    public static OrderItem createOrderItem(Order order, Product product, int quantity, int price) {
        OrderItem item = OrderItem.builder()
                .order(order)
                .product(product)
                .quantity(quantity)
                .price(price)
                .totalPrice(price * quantity)
                .build();

        return item;
    }

    // ---------------------
    // ★ Order ←→ OrderItem 양방향 연결 setter
    // ---------------------
    public void setOrder(Order order) {
        this.order = order;
    }
}
