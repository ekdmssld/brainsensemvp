package com.example.demo.controller;

import com.example.demo.dto.ProductDTO;
import com.example.demo.entity.User;
import com.example.demo.service.WishlistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/wishlist")
@RequiredArgsConstructor
@Slf4j
public class WishlistController {

    private final WishlistService wishlistService;

    // 위시리스트 페이지
    @GetMapping
    public String wishlist(@AuthenticationPrincipal User user, Model model) {
        List<ProductDTO> wishlistProducts = wishlistService.getUserWishlist(user);

        model.addAttribute("products", wishlistProducts);
        model.addAttribute("user", user);

        log.info("위시리스트 조회 - userId: {}, 상품 개수: {}", user.getId(), wishlistProducts.size());
        return "mypage/wishlist";
    }

    // ✅ 위시리스트에 추가 - redirectAttributes 사용
    @PostMapping("/add/{productId}")
    public String addToWishlist(
            @PathVariable Long productId,
            @AuthenticationPrincipal User user,
            RedirectAttributes redirectAttributes) {

        try {
            wishlistService.addToWishlist(user, productId);
            redirectAttributes.addFlashAttribute("message", "위시리스트에 추가되었습니다.");
            redirectAttributes.addFlashAttribute("alertType", "success");
            return "redirect:/products/" + productId;

        } catch (IllegalStateException e) {
            log.warn("위시리스트 추가 실패 - 이미 존재: userId={}, productId={}", user.getId(), productId);
            redirectAttributes.addFlashAttribute("message", "이미 위시리스트에 추가된 상품입니다.");
            redirectAttributes.addFlashAttribute("alertType", "warning");
            return "redirect:/products/" + productId;

        } catch (IllegalArgumentException e) {
            log.error("위시리스트 추가 실패 - 상품 없음: productId={}", productId);
            redirectAttributes.addFlashAttribute("message", "상품을 찾을 수 없습니다.");
            redirectAttributes.addFlashAttribute("alertType", "danger");
            return "redirect:/products";

        } catch (Exception e) {
            log.error("위시리스트 추가 실패", e);
            redirectAttributes.addFlashAttribute("message", "위시리스트 추가 중 오류가 발생했습니다.");
            redirectAttributes.addFlashAttribute("alertType", "danger");
            return "redirect:/products/" + productId;
        }
    }

    // 위시리스트에서 제거
    @PostMapping("/remove/{productId}")
    public String removeFromWishlist(
            @PathVariable Long productId,
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String redirect,
            RedirectAttributes redirectAttributes) {

        try {
            wishlistService.removeFromWishlist(user, productId);
            redirectAttributes.addFlashAttribute("message", "위시리스트에서 제거되었습니다.");
            redirectAttributes.addFlashAttribute("alertType", "success");

            if ("detail".equals(redirect)) {
                return "redirect:/products/" + productId;
            }
            return "redirect:/wishlist";

        } catch (Exception e) {
            log.error("위시리스트 제거 실패", e);
            redirectAttributes.addFlashAttribute("message", "위시리스트 제거 중 오류가 발생했습니다.");
            redirectAttributes.addFlashAttribute("alertType", "danger");
            return "redirect:/wishlist";
        }
    }
}