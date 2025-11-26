package com.example.demo.entity;

import com.example.demo.entity.OrderItem;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private User member; // 주문자

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    @Column(nullable = false)
    private Integer totalPrice; // 총 금액

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING; // 주문 상태

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime orderDate = LocalDateTime.now(); // 주문일

    @Column(nullable = false, length = 200)
    private String deliveryAddress; // 배송지

    @Column(length = 100)
    private String recipientName; // 수령인

    @Column(length = 20)
    private String recipientPhone; // 수령인 연락처

    @Column(length = 100)
    private String trackingNumber; // 송장번호

    @Column
    private LocalDateTime paymentDate; // 결제일

    @Column
    private LocalDateTime deliveryDate; // 배송완료일

    @Column(updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now(); // 생성일

    @Column
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now(); // 수정일

    // 주문 상태 Enum
    public enum OrderStatus {
        PENDING,        // 주문 대기
        CONFIRMED,      // 주문 확인
        PREPARING,      // 상품 준비중
        SHIPPED,        // 배송중
        DELIVERED,      // 배송완료
        CANCELLED,      // 취소됨
        REFUNDED        // 환불됨
    }

    // 비즈니스 메서드
    public void addOrderItem(OrderItem orderItem) {
        this.orderItems.add(orderItem);
        orderItem.setOrder(this);
        this.updatedAt = LocalDateTime.now();
    }

    public void changeStatus(OrderStatus status) {
        this.status = status;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
        this.updatedAt = LocalDateTime.now();
    }

    public void completeDelivery() {
        this.status = OrderStatus.DELIVERED;
        this.deliveryDate = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void cancelOrder() {
        if (this.status == OrderStatus.SHIPPED || this.status == OrderStatus.DELIVERED) {
            throw new IllegalStateException("배송 중이거나 배송 완료된 주문은 취소할 수 없습니다.");
        }
        this.status = OrderStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();

        // 재고 복구
        for (OrderItem orderItem : orderItems) {
            orderItem.getProduct().addStock(orderItem.getQuantity());
        }
    }

    public void refundOrder() {
        this.status = OrderStatus.REFUNDED;
        this.updatedAt = LocalDateTime.now();

        // 재고 복구
        for (OrderItem orderItem : orderItems) {
            orderItem.getProduct().addStock(orderItem.getQuantity());
        }
    }

    public static Order createOrder(User member, String deliveryAddress, String recipientName, String recipientPhone) {
        return Order.builder()
                .member(member)
                .deliveryAddress(deliveryAddress)
                .recipientName(recipientName)
                .recipientPhone(recipientPhone)
                .totalPrice(0)
                .build();
    }

    public void calculateTotalPrice() {
        this.totalPrice = orderItems.stream()
                .mapToInt(OrderItem::getTotalPrice)
                .sum();
    }
}