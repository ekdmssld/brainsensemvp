package com.example.demo.repository;

import com.example.demo.entity.User;
import com.example.demo.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    // 사용자의 위시리스트 조회
    List<Wishlist> findByUser(User user);

    // 사용자의 특정 상품 위시리스트 존재 확인
    Optional<Wishlist> findByUserAndProductId(User user, Long productId);

    // 특정 상품이 위시리스트에 있는지 확인
    boolean existsByUserAndProductId(User user, Long productId);

    // 사용자의 위시리스트 개수
    long countByUser(User user);
}