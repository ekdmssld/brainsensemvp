package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", unique = true)
    private Order order; // 주문

    @Column(nullable = false, length = 50)
    private String method; // 결제 수단 (카드, 계좌이체, 무통장입금 등)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING; // 결제 상태

    @Column
    private LocalDateTime paidAt; // 결제 완료 시각

    @Column
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now(); // 생성일

    // 결제 상태 Enum
    public enum PaymentStatus {
        PENDING,        // 결제 대기
        COMPLETED,      // 결제 완료
        FAILED,         // 결제 실패
        CANCELLED,      // 결제 취소
        REFUNDED        // 환불됨
    }

    // 비즈니스 메서드
    public void completePayment() {
        this.status = PaymentStatus.COMPLETED;
        this.paidAt = LocalDateTime.now();
    }

    public void failPayment() {
        this.status = PaymentStatus.FAILED;
    }

    public void cancelPayment() {
        this.status = PaymentStatus.CANCELLED;
    }

    public void refundPayment() {
        this.status = PaymentStatus.REFUNDED;
    }

    public void changeStatus(PaymentStatus status) {
        this.status = status;
        if (status == PaymentStatus.COMPLETED && this.paidAt == null) {
            this.paidAt = LocalDateTime.now();
        }
    }

    public static Payment createPayment(Order order, String method) {
        return Payment.builder()
                .order(order)
                .method(method)
                .build();
    }
}