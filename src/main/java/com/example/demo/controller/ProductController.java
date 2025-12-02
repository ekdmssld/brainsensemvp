package com.example.demo.controller;

import com.example.demo.dto.ProductDTO;
import com.example.demo.dto.ReviewDTO;
import com.example.demo.entity.Category;
import com.example.demo.entity.User;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.service.CategoryService;
import com.example.demo.service.ProductService;
import com.example.demo.service.ReviewService;
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
    private final ReviewService reviewService;

    @GetMapping("/{id}")
    public String detail(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String success,
            @RequestParam(required = false) String error,
            Model model) {

        ProductDTO product = productService.getProductById(id);

        // 위시리스트 여부 확인
        boolean isInWishlist = false;
        if (user != null) {
            isInWishlist = wishlistService.isInWishlist(user, id);
        }

        //리뷰 정보 추가
        List<ReviewDTO> reviews = reviewService.getProductReviews(id);
        Double averageRating = reviewService.getAverageRating(id);
        long reviewCount = reviewService.getReviewCount(id);

        boolean canWriteReview = false;
        if(user != null){
            canWriteReview = reviewService.canWriteReview(user.getUsername(), id);
        }

        if(user != null){
            for(ReviewDTO review : reviews){
                review.setEditable(review.getMemberName().equals(user.getUsername()) );

                review.getComments().forEach(comment ->
                        comment.setDeletable(comment.getMemberName().equals(user.getUsername()))
                );
            }
        }

        model.addAttribute("product", product);
        model.addAttribute("user", user);
        model.addAttribute("isInWishlist", isInWishlist);
        model.addAttribute("reviews", reviews);
        model.addAttribute("averageRating", String.format("%.1f", averageRating));
        model.addAttribute("reviewCount", reviewCount);
        model.addAttribute("canWriteReview", canWriteReview);

        //   성공/오류 메시지 처리
        if (success != null) {
            switch (success) {
                case "wishlist_added":
                    model.addAttribute("successMessage", "위시리스트에 추가되었습니다!");
                    break;
                case "wishlist_removed":
                    model.addAttribute("successMessage", "위시리스트에서 제거되었습니다!");
                    break;
                case "cart_added":
                    model.addAttribute("successMessage", "장바구니에 추가되었습니다!");
                    break;
            }
        }

        if (error != null) {
            switch (error) {
                case "wishlist_exists":
                    model.addAttribute("errorMessage", "이미 위시리스트에 추가된 상품입니다.");
                    break;
            }
        }

        return "products/detail";
    }

}
