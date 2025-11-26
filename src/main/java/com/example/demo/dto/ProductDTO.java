package com.example.demo.dto;

import com.example.demo.entity.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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

    //Entity -> DTO 변환
    public static ProductDTO fromEntity(Product product){
        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .description(product.getDescription())
                .imageUrl(product.getImageUrl())
                .stock(product.getStock())
                .categoryName(product.getCategory().getId())
                .manufacturer(product.getManufacturer())
                .modelNumber(product.getModelNumber())
                .isAvailable(product.getIsAvailable())
                .createdAt(product.getCreatedAt())
                .build();
    }

    public String getFormattedPrice(){
        if(stock == null || stock == 0)
            return "품절";
        else if (stock < 5)
            return "재고 부족";
        return "재고 있음";
    }
}
