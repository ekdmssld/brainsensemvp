package com.example.demo.dto;

import com.example.demo.entity.Cart;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.text.NumberFormat;
import java.util.Locale;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartDTO {

    // 장바구니 항목 id
    private Long id;

    // 상품 정보
    private Long productId;
    private String productName;
    private String productImage;  // ✅ 추가
    private Integer price;      // 상품 단가

    // 장바구니 수량
    private Integer quantity;

    // 총 금액 (단가 × 수량)
    private Integer totalPrice;

    // 엔티티 → DTO 변환
    public static CartDTO fromEntity(Cart cart) {
        Integer price = cart.getProduct().getPrice();
        Integer quantity = cart.getQuantity();

        return CartDTO.builder()
                .id(cart.getId())
                .productId(cart.getProduct().getId())
                .productName(cart.getProduct().getName())
                .productImage(cart.getProduct().getImageUrl())  // ✅ 추가
                .price(price)
                .quantity(quantity)
                .totalPrice(price * quantity)
                .build();
    }

    // 가격 포맷팅
    public String getFormattedPrice() {
        return NumberFormat.getNumberInstance(Locale.KOREA).format(price) + "원";
    }

    // 총 금액 포맷팅
    public String getFormattedTotalPrice() {
        return NumberFormat.getNumberInstance(Locale.KOREA).format(getTotalPrice()) + "원";
    }

    // Service에서 쓰는 getTotalPrice()
    public Integer getTotalPrice() {
        if (totalPrice != null) {
            return totalPrice;
        }
        if (price == null || quantity == null) {
            return 0;
        }
        return price * quantity;
    }
}