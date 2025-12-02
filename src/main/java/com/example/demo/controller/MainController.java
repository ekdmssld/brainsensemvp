package com.example.demo.controller;

import com.example.demo.dto.CategoryDTO;
import com.example.demo.dto.ProductDTO;
import com.example.demo.entity.Category;
import com.example.demo.entity.User;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.service.CategoryService;
import com.example.demo.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private final CategoryRepository categoryRepository;

    @GetMapping("/")
    public String home(
            @AuthenticationPrincipal User user,
            Model model) {

        // 최신 상품 12개 표시
        List<ProductDTO> products = productService.getLatestProducts(12);
        List<Category> categories = categoryRepository.findByParentIsNull();

        model.addAttribute("products", products);
        model.addAttribute("categories", categories);
        model.addAttribute("user", user);  //   추가

        log.info("메인 페이지 - 상품 {}개", products.size());
        return "index";
    }

    @GetMapping("/products")
    public String productList(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,  //   한 페이지에 12개씩
            @AuthenticationPrincipal User user,
            Model model) {

        Page<ProductDTO> productPage;

        if (search != null && !search.isEmpty()) {
            productPage = productService.searchProducts(search, PageRequest.of(page, size));
            log.info("상품 검색 - keyword: {}, 결과: {}개", search, productPage.getTotalElements());
        } else if (category != null) {
            productPage = productService.getProductsByCategory(category, PageRequest.of(page, size));
            log.info("카테고리 조회 - categoryId: {}, 결과: {}개", category, productPage.getTotalElements());
        } else {
            productPage = productService.getAllAvailableProducts(PageRequest.of(page, size));
            log.info("전체 상품 조회 - 결과: {}개", productPage.getTotalElements());
        }

        List<Category> categories = categoryRepository.findByParentIsNull();

        model.addAttribute("products", productPage);
        model.addAttribute("categories", categories);
        model.addAttribute("currentPage", page);
        model.addAttribute("prevPage", page > 0 ? page - 1 : 0);
        model.addAttribute("nextPage", page + 1);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("search", search == null ? "" : search);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("user", user);

        return "products/list";
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
