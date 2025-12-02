package com.example.demo.controller;

import com.example.demo.dto.InquiryDTO;
import com.example.demo.dto.OrderDTO;
import com.example.demo.dto.ProductDTO;
import com.example.demo.entity.Category;
import com.example.demo.entity.Inquiry;
import com.example.demo.entity.Order;
import com.example.demo.entity.User;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.OrderRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.InquiryService;
import com.example.demo.service.OrderService;
import com.example.demo.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")  // 관리자만 접근 가능
public class AdminController {

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final OrderService orderService;
    private final CategoryRepository categoryRepository;
    private final InquiryService inquiryService;

    /**
     * 관리자 대시보드
     * GET /admin
     */
    @GetMapping
    public String dashboard(@AuthenticationPrincipal User user, Model model) {
        log.info("관리자 대시보드 접근");

        long totalProducts = productRepository.count();
        long totalOrders = orderRepository.count();
        long totalUsers = userRepository.count();
        long pendingInquiryCount = inquiryService.getPendingInquiryCount();

        Pageable pageable = PageRequest.of(0, 5, Sort.by("createdAt").descending());
        List<InquiryDTO> recentInquiries = inquiryService.getAllInquiries(pageable).getContent();
        List<OrderDTO> recentOrders = orderRepository.findAll().stream()
                .limit(5)
                .map(OrderDTO::fromEntity)
                .toList();

        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("recentOrders", recentOrders);
        model.addAttribute("pendingInquiryCount", pendingInquiryCount);
        model.addAttribute("recentInquiries", recentInquiries);
        model.addAttribute("user", user);

        return "admin/dashboard";
    }

    // 주문 관리 페이지
    @GetMapping("/orders")
    public String ordersPage(Model model) {

        List<OrderDTO> orders = orderService.getAllOrders();
        model.addAttribute("orders", orders);

        return "admin/orders/list";
    }
    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable Long id, Model model) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        OrderDTO dto = OrderDTO.fromEntity(order);

        model.addAttribute("order", dto);
        return "admin/orders/detail";
    }

    // 회원 관리 페이지
    @GetMapping("/users")
    public String usersPage(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {

        List<Category> categories = categoryRepository.findByParentIsNull();

        Page<User> users;
        if (search != null && !search.isEmpty()) {
            users = userRepository.findByUsernameContainingOrEmailContaining(
                    search, search, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        } else {
            users = userRepository.findAll(PageRequest.of(page, size, Sort.by("createdAt").descending()));
        }

        model.addAttribute("users", users);
        model.addAttribute("categories", categories);
        model.addAttribute("search", search != null ? search : "");  //   null이면 빈 문자열
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", users.getTotalPages());

        log.info("회원 관리 - 총 회원: {}, 페이지: {}/{}", users.getTotalElements(), page + 1, users.getTotalPages());

        return "admin/users/list";
    }
    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String status,
            RedirectAttributes redirectAttributes
    ) {
        try {
            orderService.updateOrderStatus(id, status);
            redirectAttributes.addFlashAttribute("message", "주문 상태가 변경되었습니다.");
            redirectAttributes.addFlashAttribute("alertType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "상태 변경 중 오류가 발생했습니다.");
            redirectAttributes.addFlashAttribute("alertType", "danger");
        }

        return "redirect:/admin/orders";
    }
}