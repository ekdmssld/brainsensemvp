package com.example.demo.repository;

import com.example.demo.entity.Order;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // 사용자의 주문 목록 조회
    List<Order> findByMember(User member);

    // 사용자의 주문 목록 (최신순 정렬)
    List<Order> findByMemberOrderByOrderDateDesc(User member);

    // 사용자의 특정 상태 주문 조회
    List<Order> findByMemberAndStatus(User member, Order.OrderStatus status);

    // 주문 번호로 조회 (주문 아이템 포함)
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.orderItems WHERE o.id = :orderId")
    Order findByIdWithItems(Long orderId);

    List<Order> findAllByOrderByOrderDateDesc();
}