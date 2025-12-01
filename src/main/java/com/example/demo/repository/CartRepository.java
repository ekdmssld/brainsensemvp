package com.example.demo.repository;

import com.example.demo.entity.Cart;
import com.example.demo.entity.Product;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    // 사용자 기준 장바구니 조회
    List<Cart> findByMember(User member);

    // 특정 사용자 + 특정 상품
    Optional<Cart> findByMemberAndProduct(User member, Product product);

    // 개수 조회 (예: 중복 방지)
    long countByMember(User member);

    // 사용자 기준 삭제
    void deleteByMember(User member);
}