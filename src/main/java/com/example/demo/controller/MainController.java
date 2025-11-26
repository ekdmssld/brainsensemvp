package com.example.demo.controller;

import com.example.demo.dto.CategoryDTO;
import com.example.demo.dto.ProductDTO;
import com.example.demo.service.CategoryService;
import com.example.demo.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import org.springframework.ui.Model;


@Controller
@RequiredArgsConstructor
@Slf4j
public class MainController {
    private final ProductService productService;
    private final CategoryService categoryService;

    @GetMapping({"", "/", "/products"})
    public String mainPage(
            @RequestParam(value = "category", required = false) Long categoryId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "12") int size,
            @RequestParam(value = "search", required = false) String keyword,
            Model model
    ) {
        log.info("메인 페이지 접근 - categoryId: {}, page: {}, keyword: {}", categoryId, page, keyword);

        // 카테고리 목록 조회 (헤더 메뉴용)
        List<CategoryDTO> categories = categoryService.findAllTopCategory();
        model.addAttribute("categories", categories);

        // 상품 목록 조회
        Page<ProductDTO> productPage;

        if (keyword != null && !keyword.trim().isEmpty()) {
            // 검색어가 있으면 검색
            List<ProductDTO> searchResults = productService.searchProducts(keyword);
            model.addAttribute("products", searchResults);
            model.addAttribute("keyword", keyword);
            model.addAttribute("totalProducts", searchResults.size());
        } else if (categoryId != null) {
            // 카테고리 필터링
            productPage = productService.getProductsByCategoryWithPaging(categoryId, page, size);
            model.addAttribute("products", productPage.getContent());
            model.addAttribute("totalPages", productPage.getTotalPages());
            model.addAttribute("currentPage", page);
            model.addAttribute("selectedCategoryId", categoryId);

            // 선택된 카테고리 정보
            CategoryDTO selectedCategory = categoryService.getCategoryById(categoryId);
            model.addAttribute("selectedCategory", selectedCategory);
        } else {
            // 전체 상품 조회
            productPage = productService.getProductsWithPaging(page, size);
            model.addAttribute("products", productPage.getContent());
            model.addAttribute("totalPages", productPage.getTotalPages());
            model.addAttribute("currentPage", page);
        }

        // 최신 상품 (사이드바용)
        List<ProductDTO> latestProducts = productService.getLatestProducts(5);
        model.addAttribute("latestProducts", latestProducts);

        return "main/index";
    }

    @GetMapping("/guide")
    public String guidePage(){
        return "main/guide";
    }

    @GetMapping("/customer-service")
    public String customerServicePage(){
        return "main/customer-service";
    }

}
