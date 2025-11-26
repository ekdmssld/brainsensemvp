package com.example.demo.repository;

import com.example.demo.entity.Category;
import com.example.demo.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // 카테고리별 상품 조회
    List<Product> findByCategory(Category category);

    // 판매 가능한 상품만 조회
    List<Product> findByIsAvailableTrue();

    // 카테고리별 판매 가능한 상품 조회
    List<Product> findByCategoryAndIsAvailableTrue(Category category);

    // 상품명으로 검색 (부분 일치)
    List<Product> findByNameContaining(String keyword);

    // 페이징 처리된 상품 목록
    Page<Product> findByIsAvailableTrue(Pageable pageable);

    // 카테고리별 페이징
    Page<Product> findByCategoryAndIsAvailableTrue(Category category, Pageable pageable);

    // 가격 범위로 검색
    List<Product> findByPriceBetweenAndIsAvailableTrue(Integer minPrice, Integer maxPrice);

    // 최신 상품 조회 (created_at 기준)
    @Query("SELECT p FROM Product p WHERE p.isAvailable = true ORDER BY p.createdAt DESC")
    List<Product> findLatestProducts(Pageable pageable);
}