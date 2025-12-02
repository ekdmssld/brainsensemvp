package com.example.demo.entity;

import com.example.demo.entity.OrderItem;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private User member;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    @Column(nullable = false)
    private Integer totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate;

    @Column(name = "delivery_address", length = 200)
    private String deliveryAddress;

    @Column(name = "recipient_name", length = 50)
    private String recipientName;

    @Column(name = "recipient_phone", length = 20)
    private String recipientPhone;

    @Column(name = "tracking_number", length = 100)
    private String trackingNumber;

    public enum OrderStatus {
        PENDING,      // 주문 대기
        CONFIRMED,    // 주문 확인
        PREPARING,    // 상품 준비중
        SHIPPED,      // 배송중
        DELIVERED,    // 배송완료
        CANCELLED,    // 취소됨
        REFUNDED      // 환불됨
    }

    @PrePersist
    protected void onCreate() {
        if (orderDate == null) {
            orderDate = LocalDateTime.now();
        }
        if (status == null) {
            status = OrderStatus.PENDING;
        }
    }

    // === 비즈니스 로직 === //

    // 총 가격 설정
    public void setTotalPrice(Integer totalPrice) {
        this.totalPrice = totalPrice;
    }

    // 주문 취소 (재고 복구) - 한 번만 정의
    public void cancelOrder() {
        if (this.status == OrderStatus.DELIVERED) {
            throw new IllegalStateException("이미 배송 완료된 상품은 취소할 수 없습니다.");
        }

        if (this.status == OrderStatus.CANCELLED) {
            throw new IllegalStateException("이미 취소된 주문입니다.");
        }

        this.status = OrderStatus.CANCELLED;

        // 재고 복구
        for (OrderItem orderItem : orderItems) {
            orderItem.getProduct().addStock(orderItem.getQuantity());
        }
    }

    // 주문 환불
    public void refundOrder() {
        if (this.status != OrderStatus.DELIVERED) {
            throw new IllegalStateException("배송 완료된 상품만 환불할 수 있습니다.");
        }

        this.status = OrderStatus.REFUNDED;

        // 재고 복구
        for (OrderItem orderItem : orderItems) {
            orderItem.getProduct().addStock(orderItem.getQuantity());
        }
    }

    // 주문 상태 변경
    public void changeStatus(OrderStatus newStatus) {
        this.status = newStatus;
    }

    // 송장번호 입력
    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
        this.status = OrderStatus.SHIPPED;
    }
    public void addOrderItem(OrderItem orderItem) {
        this.orderItems.add(orderItem);
        orderItem.setOrder(this);
    }
}