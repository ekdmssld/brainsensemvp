package com.example.demo.dto;


import com.example.demo.entity.Product;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminProductDTO {
    private Long id;
    private String name;
    private String description;
    private Integer price;
    private Integer stock;
    private Long categoryId;
    private String categoryName;
    private String manufacturer;
    private String modelNumber;
    private String imageUrl;
    private Boolean isAvailable;

    // Entity -> DTO
    public static AdminProductDTO fromEntity(Product product) {
        return AdminProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stock(product.getStockQuantity())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : "")
                .manufacturer(product.getManufacturer())
                .modelNumber(product.getModelNumber())
                .imageUrl(product.getImageUrl())
                .isAvailable(product.getIsAvailable())
                .build();
    }

    // 포맷팅
    public String getFormattedPrice() {
        return String.format("%,d원", price);
    }

    public String getStockStatus() {
        if (stock == null || stock == 0) {
            return "품절";
        } else if (stock < 10) {
            return "재고 부족 (" + stock + "개)";
        } else {
            return "재고 있음 (" + stock + "개)";
        }
    }
}
