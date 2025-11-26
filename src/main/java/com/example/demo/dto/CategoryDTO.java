package com.example.demo.dto;

import com.example.demo.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDTO {
    private Long id;
    private String name;
    private String description;
    private Long parentId;
    private String parentName;
    private List<CategoryDTO> children;
    private Integer productCount;

    //Entity -> DTO 변환
    public static CategoryDTO fromEntity(Category category){
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .parentName(category.getParent() != null ? category.getParent().getName() : null)
                .children(category.getChildren() != null ?
                        category.getChildren().stream()
                                .map(CategoryDTO::fromEntity)
                                .toList() : null)
                .productCount(category.getProducts() != null ? category.getProducts().size() : 0)
                .build();
    }

    //자식 카테코리 순환 참조 방지를 위해 단순 변환 기입
    public static CategoryDTO fromEntitySimple(Category category){
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .parentName(category.getParent() != null ? category.getParent().getName() : null)
                .productCount(category.getProducts() != null ? category.getProducts().size() : 0)
                .build();
    }
}
