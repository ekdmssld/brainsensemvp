package com.example.demo.repository;

import com.example.demo.entity.Product;
import com.example.demo.entity.Review;
import com.example.demo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    // 상품별 리뷰 조회 (최신순)
    List<Review> findByProductOrderByCreatedAtDesc(Product product);

    // 상품별 리뷰 조회 (페이징)
    Page<Review> findByProductOrderByCreatedAtDesc(Product product, Pageable pageable);

    // 특정 사용자가 특정 상품에 작성한 리뷰 조회
    Optional<Review> findByMemberAndProduct(User member, Product product);

    // 특정 사용자가 특정 상품에 리뷰를 작성했는지 확인
    boolean existsByMemberAndProduct(User member, Product product);

    // 상품의 리뷰 개수
    long countByProduct(Product product);

    // 사용자가 작성한 모든 리뷰
    List<Review> findByMemberOrderByCreatedAtDesc(User member);
}