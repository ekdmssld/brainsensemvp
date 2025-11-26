package com.example.demo.controller;

import com.example.demo.dto.ProductDTO;
import com.example.demo.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {
    private final ProductService productService;

    @GetMapping("/{id}")
    public String productDetail(@PathVariable Long id, Model model){
        log.info("상품 상세 페이지 접근 productId : {} ", id);

        ProductDTO product = productService.getProductById(id);
        model.addAttribute("product", product);

        //리뷰 목록 조회
        //연관 상품 추천

        return "product/detail";
    }
}
