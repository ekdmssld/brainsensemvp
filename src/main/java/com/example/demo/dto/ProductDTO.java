package com.example.demo.dto;

import com.example.demo.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.Locale;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {

    private Long id;
    private String name;
    private Integer price;
    private String description;
    private String imageUrl;
    private Integer stock;
    private Long categoryName;
    private String manufacturer;
    private String modelNumber;
    private Boolean isAvailable;
    private LocalDateTime createdAt;

    private static String safe(String v) {
        return (v == null || v.trim().isEmpty()) ? null : v;
    }

    public static ProductDTO fromEntity(Product product) {
        return ProductDTO.builder()
                .id(product.getId())
                .name(safe(product.getName()))
                .price(product.getPrice())
                .description(safe(product.getDescription()))
                .imageUrl(safe(product.getImageUrl()))
                .stock(product.getStockQuantity())
                .categoryName(product.getCategory().getId())
                .manufacturer(safe(product.getManufacturer()))
                .modelNumber(safe(product.getModelNumber()))
                .isAvailable(product.getIsAvailable())
                .createdAt(product.getCreatedAt())
                .build();
    }

    public String getFormattedPrice() {
        NumberFormat formatter = NumberFormat.getNumberInstance(Locale.KOREA);
        return formatter.format(price) + "원";
    }

    public boolean hasStock() {
        return stock != null && stock > 0;
    }
}