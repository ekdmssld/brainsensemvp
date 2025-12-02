package com.example.demo.controller;

import com.example.demo.dto.OrderDTO;
import com.example.demo.dto.ProductDTO;
import com.example.demo.entity.Category;
import com.example.demo.entity.User;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.service.CartService;
import com.example.demo.service.OrderService;
import com.example.demo.service.WishlistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/mypage")
@RequiredArgsConstructor
@Slf4j
public class MyPageController {

    private final WishlistService wishlistService;
    private final CartService cartService;
    private final CategoryRepository categoryRepository;
    private final OrderService orderService;

    // 마이페이지 메인
    @GetMapping
    public String mypage(
            @AuthenticationPrincipal User user,
            Model model) {

        List<Category> categories = categoryRepository.findByParentIsNull();
        long wishlistCount = wishlistService.getWishlistCount(user);
        long cartCount = cartService.getCartCount(user);

        model.addAttribute("user", user);
        model.addAttribute("categories", categories);
        model.addAttribute("wishlistCount", wishlistCount);
        model.addAttribute("cartCount", cartCount);

        return "mypage/index";
    }

    // 마이페이지 - 위시리스트
    @GetMapping("/wishlist")
    public String mypageWishlist(
            @AuthenticationPrincipal User user,
            Model model) {

        List<ProductDTO> wishlistProducts = wishlistService.getUserWishlist(user);
        List<Category> categories = categoryRepository.findByParentIsNull();

        model.addAttribute("user", user);
        model.addAttribute("products", wishlistProducts);
        model.addAttribute("categories", categories);

        return "mypage/wishlist";
    }

    //주문 내역 확인
    @GetMapping("/orders")
    public String orders(@AuthenticationPrincipal User user, Model model) {

        List<OrderDTO> orders = orderService.getOrdersByUsername(user.getUsername());

        model.addAttribute("orders", orders);
        model.addAttribute("user", user);

        return "mypage/orders";
    }
}