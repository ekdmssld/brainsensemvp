package com.example.demo.service;

import com.example.demo.dto.CategoryDTO;
import com.example.demo.entity.Category;
import com.example.demo.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // 모든 카테고리 조회
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(CategoryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // 최상위 카테고리만 조회
    public List<CategoryDTO> getTopCategories() {
        return categoryRepository.findByParentIsNull().stream()
                .map(CategoryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // 최상위 카테고리와 하위 카테고리 함께 조회
    public List<CategoryDTO> findAllTopCategory() {
        return categoryRepository.findAllTopCategory().stream()
                .map(CategoryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // 특정 카테고리의 하위 카테고리 조회
    public List<CategoryDTO> getChildCategories(Long parentId) {
        Category parent = categoryRepository.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다: " + parentId));

        return categoryRepository.findByParent(parent).stream()
                .map(CategoryDTO::fromEntitySimple)
                .collect(Collectors.toList());
    }

    // 카테고리 상세 조회
    public CategoryDTO getCategoryById(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다: " + categoryId));

        return CategoryDTO.fromEntity(category);
    }

    // 카테고리 이름으로 조회
    public CategoryDTO getCategoryByName(String name) {
        Category category = categoryRepository.findByName(name);
        if (category == null) {
            throw new IllegalArgumentException("카테고리를 찾을 수 없습니다: " + name);
        }
        return CategoryDTO.fromEntity(category);
    }
}