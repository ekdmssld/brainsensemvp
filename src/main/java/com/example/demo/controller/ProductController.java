package com.example.demo.controller;

import com.example.demo.dto.ProductDTO;
import com.example.demo.entity.Category;
import com.example.demo.entity.User;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.service.CategoryService;
import com.example.demo.service.ProductService;
import com.example.demo.service.WishlistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {
    private final ProductService productService;
    private final CategoryRepository categoryRepository;
    private final WishlistService wishlistService;

    @GetMapping("/{id}")
    public String productDetail(
            @PathVariable Long id,
            @RequestParam(required = false) String success,
            @AuthenticationPrincipal User user,
            Model model) {

        ProductDTO product = productService.getProductById(id);
        List<Category> categories = categoryRepository.findByParentIsNull();

        // 위시리스트 여부
        boolean isInWishlist = false;
        if (user != null) {
            isInWishlist = wishlistService.isInWishlist(user, id);
        }

        // 성공 메시지 처리
        if ("wishlist_added".equals(success)) {
            model.addAttribute("successMessage", "위시리스트에 추가되었습니다.");
        } else if ("wishlist_removed".equals(success)) {
            model.addAttribute("successMessage", "위시리스트에서 제거되었습니다.");
        } else if ("cart_added".equals(success)) {
            model.addAttribute("successMessage", "장바구니에 추가되었습니다.");
        }

        model.addAttribute("product", product);
        model.addAttribute("categories", categories);
        model.addAttribute("isInWishlist", isInWishlist);
        model.addAttribute("user", user);

        // 🔥 중요! Mustache에서 로그인 여부 판단 가능하도록
        model.addAttribute("isLoggedIn", user != null);

        log.info("상품 상세 조회 - ID: {}, 이름: {}, 위시리스트: {}", id, product.getName(), isInWishlist);

        return "products/detail";
    }

}
