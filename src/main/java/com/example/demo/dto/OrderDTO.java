package com.example.demo.dto;

import com.example.demo.entity.Order;
import com.example.demo.entity.OrderItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTO {
    private Long id;
    private String memberName;
    private Integer totalPrice;
    private String status;
    private String statusDisplay;
    private LocalDateTime orderDate;
    private String deliveryAddress;
    private String recipientName;
    private String recipientPhone;
    private String trackingNumber;
    private List<OrderItemDto> orderItems;

    // === 내부 클래스: 주문 아이템 DTO === //
    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class OrderItemDto {
        private Long id;
        private String productName;
        private Long productId;
        private String productImageUrl;
        private Integer quantity;
        private Integer price;
        private Integer totalPrice;

        // OrderItem Entity -> OrderItemDto 변환
        public static OrderItemDto fromEntity(OrderItem item) {
            return OrderItemDto.builder()
                    .id(item.getId())
                    .productName(item.getProduct().getName())
                    .productId(item.getProduct().getId())
                    .productImageUrl(item.getProduct().getImageUrl())
                    .quantity(item.getQuantity())
                    .price(item.getPrice())
                    .totalPrice(item.getTotalPrice())
                    .build();
        }

        public String getFormattedPrice() {
            return String.format("%,d원", price);
        }

        public String getFormattedTotalPrice() {
            return String.format("%,d원", totalPrice);
        }
    }

    // === Order Entity -> OrderDto 변환 === //
    public static OrderDTO fromEntity(Order order) {
        return OrderDTO.builder()
                .id(order.getId())
                .memberName(order.getMember().getUsername())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus().name())
                .statusDisplay(getStatusDisplay(order.getStatus()))
                .orderDate(order.getOrderDate())
                .deliveryAddress(order.getDeliveryAddress())
                .recipientName(order.getRecipientName())
                .recipientPhone(order.getRecipientPhone())
                .trackingNumber(order.getTrackingNumber())
                .orderItems(order.getOrderItems().stream()
                        .map(OrderItemDto::fromEntity)
                        .collect(Collectors.toList()))
                .build();
    }

    // === 주문 상태 한글 변환 === //
    private static String getStatusDisplay(Order.OrderStatus status) {
        switch (status) {
            case PENDING: return "주문 대기";
            case CONFIRMED: return "주문 확인";
            case PREPARING: return "상품 준비중";
            case SHIPPED: return "배송중";
            case DELIVERED: return "배송완료";
            case CANCELLED: return "취소됨";
            case REFUNDED: return "환불됨";
            default: return status.name();
        }
    }

    // === 포맷팅 메서드들 === //

    // 가격 포맷팅
    public String getFormattedPrice() {
        return String.format("%,d원", totalPrice);
    }

    // 주문일시 포맷팅 (전체)
    public String getFormattedOrderDate() {
        if (orderDate == null) return "";
        return orderDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    // 주문일자만 (날짜)
    public String getFormattedOrderDateShort() {
        if (orderDate == null) return "";
        return orderDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    // 상품 개수
    public int getProductCount() {
        return orderItems != null ? orderItems.size() : 0;
    }

    // 취소 가능 여부
    public boolean isCancellable() {
        return "PENDING".equals(status) || "CONFIRMED".equals(status);
    }
}