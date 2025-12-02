package com.example.demo.service;

import com.example.demo.dto.CartDTO;
import com.example.demo.dto.OrderDTO;
import com.example.demo.entity.*;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    // =======================================
    // ⭐ 장바구니 기반 주문 생성(결제하기)
    // =======================================
    @Transactional
    public Long createOrder(User user, List<CartDTO> cartItems) {

        if (cartItems == null || cartItems.isEmpty()) {
            throw new IllegalArgumentException("장바구니가 비어있습니다.");
        }

        Order order = Order.builder()
                .member(user)
                .totalPrice(0)
                .status(Order.OrderStatus.PENDING)
                .build();

        int total = 0;

        for (CartDTO cart : cartItems) {

            Product product = productRepository.findById(cart.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

            int quantity = cart.getQuantity();
            int price = product.getPrice();

            // OrderItem 생성
            OrderItem orderItem = OrderItem.createOrderItem(order, product, quantity, price);

            // Order와 연결
            order.addOrderItem(orderItem);

            // 재고 차감
            product.removeStock(quantity);

            total += price * quantity;
        }

        order.setTotalPrice(total);

        orderRepository.save(order);

        return order.getId();
    }

    // =======================================
    // ⭐ 장바구니 전체 주문
    // =======================================
    @Transactional
    public Long createOrderFromCart(String username, String deliveryAddress,
                                    String recipientName, String recipientPhone) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        List<Cart> cartItems = cartRepository.findByMember(user);

        if (cartItems.isEmpty()) {
            throw new IllegalArgumentException("장바구니가 비어있습니다.");
        }

        Order order = Order.builder()
                .member(user)
                .orderItems(new ArrayList<>())
                .totalPrice(0)
                .status(Order.OrderStatus.PENDING)
                .orderDate(LocalDateTime.now())
                .deliveryAddress(deliveryAddress)
                .recipientName(recipientName)
                .recipientPhone(recipientPhone)
                .build();

        int totalPrice = 0;

        for (Cart cart : cartItems) {

            Product product = cart.getProduct();
            int quantity = cart.getQuantity();
            int price = product.getPrice();

            if (!product.getIsAvailable() || product.getStockQuantity() < quantity) {
                throw new IllegalArgumentException(product.getName() + " 재고 부족");
            }

            OrderItem orderItem = OrderItem.createOrderItem(order, product, quantity, price);

            order.addOrderItem(orderItem);
            totalPrice += orderItem.getTotalPrice();
        }

        order.setTotalPrice(totalPrice);
        orderRepository.save(order);

        cartRepository.deleteByMember(user);

        return order.getId();
    }

    // =======================================
    // ⭐ 단일 상품 바로 주문
    // =======================================
    @Transactional
    public Long createOrderDirect(String username, Long productId, Integer quantity,
                                  String deliveryAddress, String recipientName, String recipientPhone) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다"));

        if (!product.getIsAvailable() || product.getStockQuantity() < quantity) {
            throw new IllegalArgumentException("재고가 부족합니다.");
        }

        Order order = Order.builder()
                .member(user)
                .orderItems(new ArrayList<>())
                .status(Order.OrderStatus.PENDING)
                .orderDate(LocalDateTime.now())
                .deliveryAddress(deliveryAddress)
                .recipientName(recipientName)
                .recipientPhone(recipientPhone)
                .build();

        OrderItem orderItem =
                OrderItem.createOrderItem(order, product, quantity, product.getPrice());

        order.addOrderItem(orderItem);
        order.setTotalPrice(orderItem.getTotalPrice());

        orderRepository.save(order);

        return order.getId();
    }

    // =======================================
    // ⭐ 주문 상세 조회
    // =======================================
    public OrderDTO getOrderById(String username, Long orderId) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다"));

        if (!order.getMember().getId().equals(user.getId())) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        return OrderDTO.fromEntity(order);
    }

    // =======================================
    // ⭐ 사용자 주문 목록 조회
    // =======================================
    public List<OrderDTO> getOrderList(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        return orderRepository.findByMemberOrderByOrderDateDesc(user).stream()
                .map(OrderDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // =======================================
    // ⭐ 주문 취소
    // =======================================
    @Transactional
    public void cancelOrder(String username, Long orderId) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다"));

        if (!order.getMember().getId().equals(user.getId())) {
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        order.cancelOrder();
    }

    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc().stream()
                .map(OrderDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<OrderDTO> getOrdersByUsername(String username) {
        List<Order> orders = orderRepository.findByMember_Username(username);

        return orders.stream()
                .map(OrderDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public int countOrdersByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        return orderRepository.countByMember(user);
    }



}
