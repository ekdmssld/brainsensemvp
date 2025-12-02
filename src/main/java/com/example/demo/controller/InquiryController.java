package com.example.demo.controller;

import com.example.demo.dto.InquiryCommentDTO;
import com.example.demo.dto.InquiryDTO;
import com.example.demo.entity.Inquiry;
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

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/inquiry")
@RequiredArgsConstructor
@Slf4j
public class InquiryController {

    private final InquiryService inquiryService;

    // 문의 목록 (전체 공개)
    @GetMapping
    public String list(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Pageable pageable = PageRequest.of(page, 10, Sort.by("createdAt").descending());

        // 전체 문의 조회
        Page<InquiryDTO> inquiries = inquiryService.getAllInquiries(pageable);

        model.addAttribute("inquiries", inquiries);
        model.addAttribute("currentPage", page);
        model.addAttribute("user", user);

        return "inquiry/list";
    }

    // 문의 상세
    @GetMapping("/{id}")
    public String detail(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            Model model) {

        InquiryDTO inquiry = inquiryService.getInquiryById(id);

        // 열람 제한 제거 (누구나 볼 수 있음)
        // 단, 수정/삭제는 작성자만 가능

        boolean isPending = "PENDING".equals(inquiry.getStatus());
        model.addAttribute("isPending", isPending);

        List<InquiryCommentDTO> comments = inquiryService.getComments(id);
        for (InquiryCommentDTO c : comments) {
            c.setDeletable(c.getUserId().equals(user.getId()));
        }

        model.addAttribute("inquiry", inquiry);
        model.addAttribute("comments", comments);
        model.addAttribute("user", user);

        return "inquiry/detail";
    }

    // ✅ 문의 작성 폼 - 완전히 수정
    @GetMapping("/new")
    public String newForm(@AuthenticationPrincipal User user, Model model) {
        // 빈 InquiryDTO 객체 생성 (모든 필드 빈 문자열로 초기화)
        InquiryDTO inquiry = InquiryDTO.builder()
                .title("")
                .content("")
                .type("")
                .build();

        model.addAttribute("user", user);
        model.addAttribute("inquiry", inquiry);
        model.addAttribute("types", convertTypes(null));
        model.addAttribute("isEdit", false);  // ✅ 등록/수정 구분

        return "inquiry/form";
    }

    // 문의 등록
    @PostMapping
    public String create(
            @AuthenticationPrincipal User user,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam String type,
            RedirectAttributes redirectAttributes) {

        try {
            Inquiry inquiry = inquiryService.createInquiry(user.getUsername(), title, content, type);
            redirectAttributes.addFlashAttribute("message", "문의가 성공적으로 등록되었습니다.");
            redirectAttributes.addFlashAttribute("alertType", "success");
            return "redirect:/inquiry/" + inquiry.getId();

        } catch (Exception e) {
            log.error("문의 등록 실패", e);
            redirectAttributes.addFlashAttribute("message", "문의 등록 중 오류가 발생했습니다.");
            redirectAttributes.addFlashAttribute("alertType", "danger");
            return "redirect:/inquiry/new";
        }
    }

    // ✅ 문의 수정 폼 - 완전히 수정
    @GetMapping("/{id}/edit")
    public String editForm(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            Model model) {

        InquiryDTO inquiry = inquiryService.getInquiryById(id);

        if (!inquiry.getUsername().equals(user.getUsername())) {
            return "redirect:/inquiry?error=unauthorized";
        }
        if (!"PENDING".equals(inquiry.getStatus())) {
            return "redirect:/inquiry/" + id + "?error=cannot_edit";
        }

        model.addAttribute("inquiry", inquiry);
        model.addAttribute("user", user);
        model.addAttribute("types", convertTypes(inquiry.getType()));
        model.addAttribute("isEdit", true);  // ✅ 등록/수정 구분

        return "inquiry/form";
    }

    // 문의 수정
    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam String type,
            RedirectAttributes redirectAttributes) {

        try {
            inquiryService.updateInquiry(id, title, content, type, user.getUsername());
            redirectAttributes.addFlashAttribute("message", "문의가 성공적으로 수정되었습니다.");
            redirectAttributes.addFlashAttribute("alertType", "success");
            return "redirect:/inquiry/" + id;

        } catch (Exception e) {
            log.error("문의 수정 실패", e);
            redirectAttributes.addFlashAttribute("message", e.getMessage());
            redirectAttributes.addFlashAttribute("alertType", "danger");
            return "redirect:/inquiry/" + id + "/edit";
        }
    }

    // 문의 삭제
    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            RedirectAttributes redirectAttributes) {

        try {
            inquiryService.deleteInquiry(id, user.getUsername());
            redirectAttributes.addFlashAttribute("message", "문의가 성공적으로 삭제되었습니다.");
            redirectAttributes.addFlashAttribute("alertType", "success");
            return "redirect:/inquiry";

        } catch (Exception e) {
            log.error("문의 삭제 실패", e);
            redirectAttributes.addFlashAttribute("message", e.getMessage());
            redirectAttributes.addFlashAttribute("alertType", "danger");
            return "redirect:/inquiry/" + id;
        }
    }

    // 댓글 등록
    @PostMapping("/{id}/comment")
    public String createComment(
            @PathVariable Long id,
            @AuthenticationPrincipal User user,
            @RequestParam String content,
            RedirectAttributes redirectAttributes) {

        try {
            inquiryService.createComment(id, user.getUsername(), content);
            redirectAttributes.addFlashAttribute("message", "댓글이 등록되었습니다.");
            redirectAttributes.addFlashAttribute("alertType", "success");
            return "redirect:/inquiry/" + id;

        } catch (Exception e) {
            log.error("댓글 등록 실패", e);
            redirectAttributes.addFlashAttribute("message", "댓글 등록 중 오류가 발생했습니다.");
            redirectAttributes.addFlashAttribute("alertType", "danger");
            return "redirect:/inquiry/" + id;
        }
    }

    // 댓글 삭제
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
            return "redirect:/inquiry/" + inquiryId;

        } catch (Exception e) {
            log.error("댓글 삭제 실패", e);
            redirectAttributes.addFlashAttribute("message", e.getMessage());
            redirectAttributes.addFlashAttribute("alertType", "danger");
            return "redirect:/inquiry/" + inquiryId;
        }
    }

    // 타입 표시를 위한 DTO 변환
    private List<TypeOption> convertTypes(String currentType) {
        List<TypeOption> list = new ArrayList<>();

        for (Inquiry.InquiryType t : Inquiry.InquiryType.values()) {
            list.add(new TypeOption(
                    t.name(),
                    t.getDescription(),
                    t.name().equals(currentType)
            ));
        }

        return list;
    }

    @lombok.AllArgsConstructor
    @lombok.Getter
    public static class TypeOption {
        private String name;
        private String description;
        private boolean selected;
    }
}