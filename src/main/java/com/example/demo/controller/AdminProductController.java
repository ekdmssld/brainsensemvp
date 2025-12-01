package com.example.demo.controller;

import com.example.demo.dto.AdminProductDTO;
import com.example.demo.entity.Category;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {

    private final ProductService productService;
    private final CategoryRepository categoryRepository;

    // 상품 목록
    @GetMapping
    public String listProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model) {

        Page<AdminProductDTO> products =
                productService.getAdminProductList(search, categoryId, page, size);

        List<Category> categories = categoryRepository.findByParentIsNull();

        model.addAttribute("products", products);
        model.addAttribute("categories", categories);

        model.addAttribute("search", search == null ? "" : search);
        model.addAttribute("categoryId", categoryId == null ? 0 : categoryId);

        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", products.getTotalPages());

        return "admin/products/list";
    }

    // 새 상품 등록 폼
    @GetMapping("/new")
    public String newProductForm(Model model) {

        List<Category> categories = categoryRepository.findAll();

        AdminProductDTO product = AdminProductDTO.builder()
                .name("")
                .description("")
                .price(0)
                .stock(0)
                .manufacturer("")
                .modelNumber("")
                .isAvailable(true)
                .build();

        model.addAttribute("product", product);
        model.addAttribute("categories", categories);
        model.addAttribute("isEdit", false);

        return "admin/products/form";
    }

    // 상품 생성 처리
    @PostMapping
    public String createProduct(
            @ModelAttribute AdminProductDTO dto,
            @RequestParam(required = false) MultipartFile imageFile,
            RedirectAttributes redirectAttributes) {

        try {
            productService.createProduct(dto, imageFile);

            redirectAttributes.addFlashAttribute("message", "상품이 성공적으로 등록되었습니다.");
            redirectAttributes.addFlashAttribute("isSuccess", true);
            redirectAttributes.addFlashAttribute("isDanger", false);

            return "redirect:/admin/products";

        } catch (Exception e) {
            log.error("상품 등록 실패", e);

            redirectAttributes.addFlashAttribute("message",
                    "상품 등록 중 오류가 발생했습니다: " + e.getMessage());
            redirectAttributes.addFlashAttribute("isSuccess", false);
            redirectAttributes.addFlashAttribute("isDanger", true);

            return "redirect:/admin/products/new";
        }
    }

    // 상품 상세
    @GetMapping("/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        AdminProductDTO product = productService.getAdminProductById(id);
        model.addAttribute("product", product);
        return "admin/products/detail";
    }

    // 상품 수정 폼
    @GetMapping("/{id}/edit")
    public String editProductForm(@PathVariable Long id, Model model) {

        AdminProductDTO product = productService.getAdminProductById(id);
        List<Category> categories = categoryRepository.findAll();

        model.addAttribute("product", product);
        model.addAttribute("categories", categories);
        model.addAttribute("isEdit", true);

        return "admin/products/form";
    }

    // 상품 수정 처리
    @PostMapping("/{id}")
    public String updateProduct(
            @PathVariable Long id,
            @ModelAttribute AdminProductDTO dto,
            @RequestParam(required = false) MultipartFile imageFile,
            RedirectAttributes redirectAttributes) {

        try {
            productService.updateProduct(id, dto, imageFile);

            redirectAttributes.addFlashAttribute("message", "상품이 성공적으로 수정되었습니다.");
            redirectAttributes.addFlashAttribute("isSuccess", true);
            redirectAttributes.addFlashAttribute("isDanger", false);

            return "redirect:/admin/products";

        } catch (Exception e) {
            log.error("상품 수정 실패 - ID: {}", id, e);

            redirectAttributes.addFlashAttribute("message",
                    "상품 수정 중 오류가 발생했습니다: " + e.getMessage());
            redirectAttributes.addFlashAttribute("isSuccess", false);
            redirectAttributes.addFlashAttribute("isDanger", true);

            return "redirect:/admin/products/" + id + "/edit";
        }
    }

    // 상품 삭제
    @PostMapping("/{id}/delete")
    public String deleteProduct(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            productService.deleteProduct(id);

            redirectAttributes.addFlashAttribute("message", "상품이 성공적으로 삭제되었습니다.");
            redirectAttributes.addFlashAttribute("isSuccess", true);
            redirectAttributes.addFlashAttribute("isDanger", false);

        } catch (Exception e) {
            log.error("상품 삭제 실패 - ID: {}", id, e);

            redirectAttributes.addFlashAttribute("message",
                    "상품 삭제 중 오류가 발생했습니다: " + e.getMessage());
            redirectAttributes.addFlashAttribute("isSuccess", false);
            redirectAttributes.addFlashAttribute("isDanger", true);
        }

        return "redirect:/admin/products";
    }

    // 재고 조정
    @PostMapping("/{id}/stock")
    public String adjustStock(
            @PathVariable Long id,
            @RequestParam String type,
            @RequestParam Integer quantity) {

        productService.adjustStock(id, type, quantity);
        return "redirect:/admin/products/" + id;
    }
}
