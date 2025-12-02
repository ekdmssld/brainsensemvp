package com.example.demo.controller;

import com.example.demo.dto.OrderDTO;
import com.example.demo.entity.Order;
import com.example.demo.entity.User;
import com.example.demo.repository.OrderRepository;
import com.example.demo.service.CartService;
import com.example.demo.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;

    /**
     * 결제 페이지
     */
    @GetMapping("/checkout")
    public String checkout(@AuthenticationPrincipal User user, Model model) {

        model.addAttribute("user", user);
        model.addAttribute("cartItems", cartService.getCartItems(user.getUsername()));
        model.addAttribute("totalPrice", cartService.getTotalPrice(user.getUsername()));

        return "order/checkout";
    }

    /**
     * 결제 완료 → 주문 생성
     */
    @PostMapping("/complete")
    public String completeOrder(@AuthenticationPrincipal User user, Model model) {

        // 기본 배송 정보로 생성
        Long orderId = orderService.createOrderFromCart(
                user.getUsername(),
                user.getAddress(),
                user.getUsername(),
                user.getPhone()
        );

        OrderDTO order = orderService.getOrderById(user.getUsername(), orderId);
        model.addAttribute("order", order);
        model.addAttribute("user", user);

        return "order/complete";
    }

    /**
     * 주문 상세 페이지
     */
    @GetMapping("/{orderId}")
    public String orderDetail(@PathVariable Long orderId,
                              @AuthenticationPrincipal User user,
                              Model model) {

        OrderDTO order = orderService.getOrderById(user.getUsername(), orderId);

        model.addAttribute("order", order);
        model.addAttribute("user", user);
        return "order/detail";
    }

    /**
     * 마이페이지 → 주문 내역 페이지
     */
    @GetMapping("/mypage")
    public String myOrders(@AuthenticationPrincipal User user, Model model) {

        // 현재 사용자 주문 리스트 조회
        List<OrderDTO> orders = orderService.getOrdersByUsername(user.getUsername());

        model.addAttribute("orders", orders);
        model.addAttribute("user", user);

        return "mypage/orders";
    }


}
