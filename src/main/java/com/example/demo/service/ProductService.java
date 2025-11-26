package com.example.demo.service;

import com.example.demo.dto.ProductDTO;
import com.example.demo.entity.Category;
import com.example.demo.entity.Product;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    // 모든 판매 가능한 상품 조회
    public List<ProductDTO> getAllAvailableProducts() {
        return productRepository.findByIsAvailableTrue().stream()
                .map(ProductDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // 페이징 처리된 상품 목록 조회
    public Page<ProductDTO> getProductsWithPaging(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return productRepository.findByIsAvailableTrue(pageable)
                .map(ProductDTO::fromEntity);
    }

    // 카테고리별 상품 조회
    public List<ProductDTO> getProductsByCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다: " + categoryId));

        return productRepository.findByCategoryAndIsAvailableTrue(category).stream()
                .map(ProductDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // 카테고리별 페이징 처리된 상품 조회
    public Page<ProductDTO> getProductsByCategoryWithPaging(Long categoryId, int page, int size) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("카테고리를 찾을 수 없습니다: " + categoryId));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return productRepository.findByCategoryAndIsAvailableTrue(category, pageable)
                .map(ProductDTO::fromEntity);
    }

    // 상품 상세 조회
    public ProductDTO getProductById(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다: " + productId));

        return ProductDTO.fromEntity(product);
    }

    // 상품 검색
    public List<ProductDTO> searchProducts(String keyword) {
        return productRepository.findByNameContaining(keyword).stream()
                .map(ProductDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // 최신 상품 조회
    public List<ProductDTO> getLatestProducts(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.findLatestProducts(pageable).stream()
                .map(ProductDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // 가격 범위로 검색
    public List<ProductDTO> getProductsByPriceRange(Integer minPrice, Integer maxPrice) {
        return productRepository.findByPriceBetweenAndIsAvailableTrue(minPrice, maxPrice).stream()
                .map(ProductDTO::fromEntity)
                .collect(Collectors.toList());
    }
}