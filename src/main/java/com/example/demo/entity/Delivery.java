package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "deliveries")
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", unique = true)
    private Order order; // 주문

    @Column(length = 100)
    private String trackingNo; // 송장번호

    @Column(nullable = false, length = 300)
    private String address; // 배송지 주소

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DeliveryState state = DeliveryState.PREPARING; // 배송 상태

    @Column
    private LocalDateTime shippedAt; // 배송 시작 시각

    @Column
    private LocalDateTime deliveredAt; // 배송 완료 시각

    @Column
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now(); // 생성일

    @Column
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now(); // 수정일

    // 배송 상태 Enum
    public enum DeliveryState {
        PREPARING,      // 배송 준비중
        SHIPPED,        // 배송중
        DELIVERED,      // 배송 완료
        RETURNED,       // 반품
        CANCELLED       // 취소됨
    }

    // 비즈니스 메서드
    public void updateTrackingNo(String trackingNo) {
        this.trackingNo = trackingNo;
        this.updatedAt = LocalDateTime.now();
    }

    public void updateAddress(String address) {
        this.address = address;
        this.updatedAt = LocalDateTime.now();
    }

    public void startShipping(String trackingNo) {
        this.trackingNo = trackingNo;
        this.state = DeliveryState.SHIPPED;
        this.shippedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void completeDelivery() {
        this.state = DeliveryState.DELIVERED;
        this.deliveredAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void returnDelivery() {
        this.state = DeliveryState.RETURNED;
        this.updatedAt = LocalDateTime.now();
    }

    public void cancelDelivery() {
        this.state = DeliveryState.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }

    public void changeState(DeliveryState state) {
        this.state = state;
        this.updatedAt = LocalDateTime.now();
    }

    public static Delivery createDelivery(Order order, String address) {
        return Delivery.builder()
                .order(order)
                .address(address)
                .build();
    }
}