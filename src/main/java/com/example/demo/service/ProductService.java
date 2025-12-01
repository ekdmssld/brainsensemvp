package com.example.demo.service;

import com.example.demo.dto.AdminProductDTO;
import com.example.demo.dto.ProductDTO;
import com.example.demo.entity.Category;
import com.example.demo.entity.Product;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    // 업로드 디렉토리
    private static final String UPLOAD_DIR = "uploads/products/";

    /**
     * 이미지 업로드 공통 메서드
     */
    private String uploadImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // 업로드 디렉토리 생성
        File uploadDir = new File(UPLOAD_DIR);
        if (!uploadDir.exists()) {
            boolean made = uploadDir.mkdirs();
            log.info("업로드 디렉토리 생성({}): {}", made, UPLOAD_DIR);
        }

        String originalFilename = file.getOriginalFilename();
        String extension = "";

        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String savedFilename = UUID.randomUUID().toString() + extension;

        Path filePath = Paths.get(UPLOAD_DIR + savedFilename);
        Files.write(filePath, file.getBytes());

        String imageUrl = "/uploads/products/" + savedFilename;
        log.info("이미지 업로드 완료 - 파일명: {}, URL: {}", savedFilename, imageUrl);

        return imageUrl;
    }

    // ========================================
    // 일반 사용자용 메서드
    // ========================================

    /**
     * 모든 판매 가능한 상품 조회 (페이징)
     */
    public Page<ProductDTO> getAllAvailableProducts(Pageable pageable) {
        Page<Product> products = productRepository.findByIsAvailableTrue(pageable);
        return products.map(ProductDTO::fromEntity);
    }

    /**
     * 상품 검색 (페이징)
     */
    public Page<ProductDTO> searchProducts(String keyword, Pageable pageable) {
        Page<Product> products = productRepository.findByNameContaining(keyword, pageable);
        return products.map(ProductDTO::fromEntity);
    }

    /**
     * 카테고리별 상품 조회 (페이징)
     */
    public Page<ProductDTO> getProductsByCategory(Long categoryId, Pageable pageable) {
        Page<Product> products = productRepository.findByCategoryId(categoryId, pageable);
        return products.map(ProductDTO::fromEntity);
    }

    /**
     * 상품 상세 조회 (ID)
     */
    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다. ID: " + id));
        return ProductDTO.fromEntity(product);
    }

    /**
     * 최신 상품 조회
     */
    public List<ProductDTO> getLatestProducts(int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Product> products = productRepository.findByIsAvailableTrue(pageable);
        return products.stream()
                .map(ProductDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // ========================================
    // 관리자용 메서드
    // ========================================

    /**
     * 관리자 상품 목록 조회 (검색, 필터링, 페이징)
     */
    public Page<AdminProductDTO> getAdminProductList(String search, Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Product> products;

        if (search != null && !search.isEmpty() && categoryId != null) {
            // 검색어 + 카테고리
            products = productRepository.findByNameContainingAndCategoryId(search, categoryId, pageable);
            log.info("검색어 + 카테고리 조회 - keyword: {}, categoryId: {}, 결과: {}개",
                    search, categoryId, products.getTotalElements());
        } else if (search != null && !search.isEmpty()) {
            // 검색어만
            products = productRepository.findByNameContaining(search, pageable);
            log.info("검색어 조회 - keyword: {}, 결과: {}개", search, products.getTotalElements());
        } else if (categoryId != null) {
            // 카테고리만
            products = productRepository.findByCategoryId(categoryId, pageable);
            log.info("카테고리 조회 - categoryId: {}, 결과: {}개", categoryId, products.getTotalElements());
        } else {
            // 전체 조회
            products = productRepository.findAll(pageable);
            log.info("전체 상품 조회 - 결과: {}개", products.getTotalElements());
        }

        return products.map(AdminProductDTO::fromEntity);
    }

    /**
     * 관리자 상품 상세 조회
     */
    public AdminProductDTO getAdminProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다. ID: " + id));
        return AdminProductDTO.fromEntity(product);
    }

    /**
     * 상품 생성
     */
    @Transactional
    public AdminProductDTO createProduct(AdminProductDTO dto, MultipartFile imageFile) throws IOException {
        log.info("상품 생성 시작 - 이름: {}", dto.getName());

        // 카테고리 조회
        Category category = null;
        if (dto.getCategoryId() != null) {
            category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다. ID: " + dto.getCategoryId()));
        }

        // 이미지 업로드
        String imageUrl = null;
        if (imageFile != null && !imageFile.isEmpty()) {
            imageUrl = uploadImage(imageFile);
        }

        // 상품 생성
        Product product = Product.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .stockQuantity(dto.getStock())            // ✅ AdminProductDTO의 stock 사용
                .category(category)
                .manufacturer(dto.getManufacturer())
                .modelNumber(dto.getModelNumber())
                .imageUrl(imageUrl)
                .isAvailable(dto.getIsAvailable() != null ? dto.getIsAvailable() : true)
                .build();

        product = productRepository.save(product);

        log.info("상품 생성 완료 - ID: {}, 이름: {}", product.getId(), product.getName());
        return AdminProductDTO.fromEntity(product);
    }

    /**
     * 상품 수정
     */
    @Transactional
    public AdminProductDTO updateProduct(Long id, AdminProductDTO dto, MultipartFile imageFile) throws IOException {
        log.info("상품 수정 시작 - ID: {}", id);

        // 상품 조회
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다. ID: " + id));

        // 카테고리 조회
        Category category = null;
        if (dto.getCategoryId() != null) {
            category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다. ID: " + dto.getCategoryId()));
        }

        // 이미지 처리
        String imageUrl = product.getImageUrl();
        if (imageFile != null && !imageFile.isEmpty()) {
            imageUrl = uploadImage(imageFile);
        }

        // 상품 정보 업데이트
        // 👉 여기서는 기존 엔티티가 가진 메서드에 맞춰서 사용
        //    (updateProduct + setStock 패턴 유지)
        product.updateProduct(
                dto.getName(),
                dto.getDescription(),
                dto.getPrice(),
                category,
                dto.getManufacturer(),
                dto.getModelNumber(),
                imageUrl
        );

        if (dto.getStock() != null) {
            product.setStock(dto.getStock());        // ✅ 재고 필드 갱신
        }

//        // isAvailable도 DTO에 값이 있다면 업데이트
//        if (dto.getIsAvailable() != null) {
//            product.setIsAvailable(dto.getIsAvailable());
//        }

        log.info("상품 수정 완료 - ID: {}, 이름: {}", product.getId(), product.getName());
        return AdminProductDTO.fromEntity(product);
    }

    /**
     * 상품 삭제
     */
    @Transactional
    public void deleteProduct(Long id) {
        log.info("상품 삭제 시작 - ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));

        productRepository.delete(product);
        log.info("상품 삭제 완료 - ID: {}", id);
    }

    /**
     * 재고 조정
     */
    @Transactional
    public void adjustStock(Long id, String type, Integer quantity) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다. ID: " + id));

        switch (type.toUpperCase()) {
            case "ADD":
                product.addStock(quantity);
                log.info("재고 추가 - 상품 ID: {}, 수량: {}, 현재 재고: {}", id, quantity, product.getStockQuantity());
                break;
            case "REMOVE":
                product.removeStock(quantity);
                log.info("재고 차감 - 상품 ID: {}, 수량: {}, 현재 재고: {}", id, quantity, product.getStockQuantity());
                break;
            case "SET":
                product.setStock(quantity);
                log.info("재고 설정 - 상품 ID: {}, 수량: {}", id, quantity);
                break;
            default:
                throw new IllegalArgumentException("잘못된 재고 조정 타입입니다: " + type);
        }
    }
}
