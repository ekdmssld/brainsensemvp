package com.example.demo.controller;

import com.example.demo.dto.ReviewDTO;
import com.example.demo.entity.User;
import com.example.demo.service.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/review")
@RequiredArgsConstructor
@Slf4j
public class ReviewController {

    private final ReviewService reviewService;

    // 리뷰 작성
    @PostMapping("/create")
    public String createReview(
            @AuthenticationPrincipal User user,
            @RequestParam Long productId,
            @RequestParam Integer rating,
            @RequestParam String content,
            RedirectAttributes redirectAttributes) {

        try {
            reviewService.createReview(user.getUsername(), productId, rating, content);
            redirectAttributes.addFlashAttribute("message", "리뷰가 등록되었습니다.");
            redirectAttributes.addFlashAttribute("alertType", "success");

        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("message", e.getMessage());
            redirectAttributes.addFlashAttribute("alertType", "danger");

        } catch (Exception e) {
            log.error("리뷰 작성 실패", e);
            redirectAttributes.addFlashAttribute("message", "리뷰 작성 중 오류가 발생했습니다.");
            redirectAttributes.addFlashAttribute("alertType", "danger");
        }

        return "redirect:/products/" + productId;
    }

    // 리뷰 수정
    @PostMapping("/{reviewId}/update")
    public String updateReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal User user,
            @RequestParam Long productId,
            @RequestParam Integer rating,
            @RequestParam String content,
            RedirectAttributes redirectAttributes) {

        try {
            reviewService.updateReview(reviewId, user.getUsername(), rating, content);
            redirectAttributes.addFlashAttribute("message", "리뷰가 수정되었습니다.");
            redirectAttributes.addFlashAttribute("alertType", "success");

        } catch (Exception e) {
            log.error("리뷰 수정 실패", e);
            redirectAttributes.addFlashAttribute("message", e.getMessage());
            redirectAttributes.addFlashAttribute("alertType", "danger");
        }

        return "redirect:/products/" + productId;
    }

    // 리뷰 삭제
    @PostMapping("/{reviewId}/delete")
    public String deleteReview(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal User user,
            @RequestParam Long productId,
            RedirectAttributes redirectAttributes) {

        try {
            reviewService.deleteReview(reviewId, user.getUsername());
            redirectAttributes.addFlashAttribute("message", "리뷰가 삭제되었습니다.");
            redirectAttributes.addFlashAttribute("alertType", "success");

        } catch (Exception e) {
            log.error("리뷰 삭제 실패", e);
            redirectAttributes.addFlashAttribute("message", e.getMessage());
            redirectAttributes.addFlashAttribute("alertType", "danger");
        }

        return "redirect:/products/" + productId;
    }

    // 리뷰 댓글 작성
    @PostMapping("/{reviewId}/comment")
    public String createComment(
            @PathVariable Long reviewId,
            @AuthenticationPrincipal User user,
            @RequestParam Long productId,
            @RequestParam String content,
            RedirectAttributes redirectAttributes) {

        try {
            reviewService.createComment(reviewId, user.getUsername(), content);
            redirectAttributes.addFlashAttribute("message", "댓글이 등록되었습니다.");
            redirectAttributes.addFlashAttribute("alertType", "success");

        } catch (Exception e) {
            log.error("댓글 작성 실패", e);
            redirectAttributes.addFlashAttribute("message", "댓글 작성 중 오류가 발생했습니다.");
            redirectAttributes.addFlashAttribute("alertType", "danger");
        }

        return "redirect:/products/" + productId;
    }

    // 리뷰 댓글 삭제
    @PostMapping("/comment/{commentId}/delete")
    public String deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal User user,
            @RequestParam Long productId,
            RedirectAttributes redirectAttributes) {

        try {
            reviewService.deleteComment(commentId, user.getUsername());
            redirectAttributes.addFlashAttribute("message", "댓글이 삭제되었습니다.");
            redirectAttributes.addFlashAttribute("alertType", "success");

        } catch (Exception e) {
            log.error("댓글 삭제 실패", e);
            redirectAttributes.addFlashAttribute("message", e.getMessage());
            redirectAttributes.addFlashAttribute("alertType", "danger");
        }

        return "redirect:/products/" + productId;
    }
}