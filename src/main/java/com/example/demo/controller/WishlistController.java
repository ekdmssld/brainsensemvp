package com.example.demo.controller;

import com.example.demo.dto.ProductDTO;
import com.example.demo.entity.Category;
import com.example.demo.entity.User;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.service.WishlistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/wishlist")
@RequiredArgsConstructor
@Slf4j
public class WishlistController {

    private final WishlistService wishlistService;
    private final CategoryRepository categoryRepository;

    // 위시리스트 페이지
    @GetMapping
    public String wishlist(@AuthenticationPrincipal User user, Model model) {
        List<ProductDTO> wishlistProducts = wishlistService.getUserWishlist(user);
        List<Category> categories = categoryRepository.findByParentIsNull();

        model.addAttribute("products", wishlistProducts);
        model.addAttribute("categories", categories);

        log.info("위시리스트 조회 - userId: {}, 상품 개수: {}", user.getId(), wishlistProducts.size());
        return "wishlist/list";
    }

    // 위시리스트에 추가
    @PostMapping("/add/{productId}")
    public String addToWishlist(
            @PathVariable Long productId,
            @AuthenticationPrincipal User user) {

        try {
            wishlistService.addToWishlist(user, productId);
            return "redirect:/products/" + productId + "?success=wishlist_added";
        } catch (IllegalStateException e) {
            return "redirect:/products/" + productId + "?error=wishlist_exists";
        }
    }

    // 위시리스트에서 제거
    @PostMapping("/remove/{productId}")
    public String removeFromWishlist(
            @PathVariable Long productId,
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String redirect) {

        wishlistService.removeFromWishlist(user, productId);

        if ("detail".equals(redirect)) {
            return "redirect:/products/" + productId + "?success=wishlist_removed";
        }
        return "redirect:/wishlist?success=removed";
    }
}