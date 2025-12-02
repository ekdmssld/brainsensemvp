package com.example.demo.controller;

import com.example.demo.dto.CartDTO;
import com.example.demo.entity.User;
import com.example.demo.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;

    /**
     * 장바구니 목록 페이지
     * GET /cart
     */
    @GetMapping
    public String cartPage(@AuthenticationPrincipal User user, @AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login?redirect=/cart";
        }

        String username = userDetails.getUsername();
        log.info("장바구니 페이지 접근 - user: {}", username);

        List<CartDTO> cartItems = cartService.getCartItems(username);
        Integer totalPrice = cartService.getTotalPrice(username);

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("formattedTotalPrice", String.format("%,d원", totalPrice));
        model.addAttribute("user", user);

        return "cart/list";
    }

    /**
     * 장바구니에 상품 추가
     * POST /cart/add
     */
    @PostMapping("/add")
    public String addToCart(
            @AuthenticationPrincipal User user,
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") Integer quantity,
            RedirectAttributes redirectAttributes) {

        try {
            cartService.addToCart(user.getUsername(), productId, quantity);
            redirectAttributes.addFlashAttribute("message", "장바구니에 추가되었습니다.");
            redirectAttributes.addFlashAttribute("alertType", "success");
            return "redirect:/products/" + productId;

        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
            redirectAttributes.addFlashAttribute("alertType", "danger");
            return "redirect:/products/" + productId;
        }
    }

    /**
     * 장바구니 수량 변경
     * POST /cart/update
     */
    @PostMapping("/update")
    public String updateQuantity(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long cartId,
            @RequestParam Integer quantity,
            RedirectAttributes redirectAttributes) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        try {
            String username = userDetails.getUsername();
            cartService.updateQuantity(username, cartId, quantity);

            redirectAttributes.addFlashAttribute("msg", "수량이 변경되었습니다.");

        } catch (Exception e) {
            log.error("수량 변경 실패", e);
            redirectAttributes.addFlashAttribute("error", "수량 변경 중 오류가 발생했습니다.");
        }

        return "redirect:/cart";
    }

    /**
     * 장바구니에서 삭제
     * POST /cart/remove
     */
    @PostMapping("/remove")
    public String removeFromCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long cartId,
            RedirectAttributes redirectAttributes) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        try {
            String username = userDetails.getUsername();
            cartService.removeFromCart(username, cartId);

            redirectAttributes.addFlashAttribute("msg", "상품이 삭제되었습니다.");

        } catch (Exception e) {
            log.error("장바구니 삭제 실패", e);
            redirectAttributes.addFlashAttribute("error", "삭제 중 오류가 발생했습니다.");
        }

        return "redirect:/cart";
    }

    /**
     * 장바구니 전체 삭제
     * POST /cart/clear
     */
    @PostMapping("/clear")
    public String clearCart(
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        if (userDetails == null) {
            return "redirect:/login";
        }

        try {
            String username = userDetails.getUsername();
            cartService.clearCart(username);

            redirectAttributes.addFlashAttribute("msg", "장바구니가 비워졌습니다.");

        } catch (Exception e) {
            log.error("장바구니 비우기 실패", e);
            redirectAttributes.addFlashAttribute("error", "오류가 발생했습니다.");
        }

        return "redirect:/cart";
    }
}