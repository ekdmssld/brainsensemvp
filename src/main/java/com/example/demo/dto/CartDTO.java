package com.example.demo.dto;

import com.example.demo.entity.Cart;
import com.example.demo.entity.Product;
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
    private String productImage;  // 상품 이미지 URL
    private Integer price;        // 상품 단가
    private Integer stock;        // 상품 재고 수량

    // 장바구니 수량
    private Integer quantity;

    // 총 금액 (단가 × 수량)
    private Integer totalPrice;

    // 엔티티 → DTO 변환
    public static CartDTO fromEntity(Cart cart) {
        Product product = cart.getProduct();
        Integer price = product.getPrice();
        Integer quantity = cart.getQuantity();

        return CartDTO.builder()
                .id(cart.getId())
                .productId(product.getId())
                .productName(product.getName())
                .productImage(product.getImageUrl())
                .price(price)
                .stock(product.getStockQuantity())  //   재고 정보 포함
                .quantity(quantity)
                .totalPrice(price * quantity)
                .build();
    }

    // 가격 포맷팅 (개당 가격)
    public String getFormattedPrice() {
        if (price == null) return "0원";
        return NumberFormat.getNumberInstance(Locale.KOREA).format(price) + "원";
    }

    // 총 금액 포맷팅
    public String getFormattedTotalPrice() {
        return NumberFormat.getNumberInstance(Locale.KOREA)
                .format(getTotalPrice()) + "원";
    }

    // 총 금액 계산 (필드에 값이 없으면 계산해서 반환)
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