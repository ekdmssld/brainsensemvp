package com.example.demo.controller;

import com.example.demo.dto.InquiryCommentDTO;
import com.example.demo.dto.InquiryDTO;
import com.example.demo.entity.User;
import com.example.demo.service.InquiryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/inquiry")
@RequiredArgsConstructor
@Slf4j
public class AdminInquiryController {

    private final InquiryService inquiryService;

    // 문의 목록 (관리자)
    @GetMapping
    public String list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String keyword,
            Model model) {

        Pageable pageable = PageRequest.of(page, 15, Sort.by("createdAt").descending());
        Page<InquiryDTO> inquiries;

        if (keyword != null && !keyword.isEmpty()) {
            inquiries = inquiryService.searchInquiries(keyword, pageable);
            model.addAttribute("keyword", keyword);
        } else {
            inquiries = inquiryService.getAllInquiries(pageable);
            model.addAttribute("keyword", "");
        }

        long pendingCount = inquiryService.getPendingInquiryCount();

        model.addAttribute("inquiries", inquiries);
        model.addAttribute("pendingCount", pendingCount);
        model.addAttribute("currentPage", page);

        log.info("관리자 문의 목록 조회 - 페이지: {}", page);
        return "admin/inquiry/list";
    }

    // 문의 상세 (관리자)
    @GetMapping("/{id}")
    public String detail(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            Model model) {

        InquiryDTO inquiry = inquiryService.getInquiryById(id);
        List<InquiryCommentDTO> comments = inquiryService.getComments(id);

        model.addAttribute("inquiry", inquiry);
        model.addAttribute("comments", comments);
        model.addAttribute("user", user);

        log.info("관리자 문의 상세 조회 - ID: {}", id);
        return "admin/inquiry/detail";
    }

    // 댓글 등록 (관리자)
    @PostMapping("/{id}/comment")
    public String createComment(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            @RequestParam String content,
            RedirectAttributes redirectAttributes) {

        try {
            inquiryService.createComment(id, user.getUsername(), content);
            redirectAttributes.addFlashAttribute("message", "답변이 등록되었습니다.");
            redirectAttributes.addFlashAttribute("alertType", "success");
            return "redirect:/admin/inquiry/" + id;

        } catch (Exception e) {
            log.error("답변 등록 실패", e);
            redirectAttributes.addFlashAttribute("message", "답변 등록 중 오류가 발생했습니다.");
            redirectAttributes.addFlashAttribute("alertType", "danger");
            return "redirect:/admin/inquiry/" + id;
        }
    }

    // 문의 상태 변경
    @PostMapping("/{id}/status")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            RedirectAttributes redirectAttributes) {

        try {
            inquiryService.updateInquiryStatus(id, status);
            redirectAttributes.addFlashAttribute("message", "문의 상태가 변경되었습니다.");
            redirectAttributes.addFlashAttribute("alertType", "success");
            return "redirect:/admin/inquiry/" + id;

        } catch (Exception e) {
            log.error("상태 변경 실패", e);
            redirectAttributes.addFlashAttribute("message", "상태 변경 중 오류가 발생했습니다.");
            redirectAttributes.addFlashAttribute("alertType", "danger");
            return "redirect:/admin/inquiry/" + id;
        }
    }

    // 댓글 삭제 (관리자)
    @PostMapping("/comment/{commentId}/delete")
    public String deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal User user,
            @RequestParam Long inquiryId,
            RedirectAttributes redirectAttributes) {

        try {
            inquiryService.deleteComment(commentId, user.getUsername());
            redirectAttributes.addFlashAttribute("message", "댓글이 삭제되었습니다.");
            redirectAttributes.addFlashAttribute("alertType", "success");
            return "redirect:/admin/inquiry/" + inquiryId;

        } catch (Exception e) {
            log.error("댓글 삭제 실패", e);
            redirectAttributes.addFlashAttribute("message", e.getMessage());
            redirectAttributes.addFlashAttribute("alertType", "danger");
            return "redirect:/admin/inquiry/" + inquiryId;
        }
    }
}