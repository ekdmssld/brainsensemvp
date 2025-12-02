package com.example.demo.service;

import com.example.demo.dto.ProductDTO;
import com.example.demo.entity.Product;
import com.example.demo.entity.User;
import com.example.demo.entity.Wishlist;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.WishlistRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;

    /**
     * 위시리스트에 상품 추가
     */
    @Transactional
    public void addToWishlist(User user, Long productId) {
        // 이미 존재하는지 확인
        if (wishlistRepository.existsByUserAndProductId(user, productId)) {
            log.info("이미 위시리스트에 존재 - userId: {}, productId: {}", user.getId(), productId);
            throw new IllegalStateException("이미 위시리스트에 추가된 상품입니다.");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

        Wishlist wishlist = Wishlist.builder()
                .user(user)
                .product(product)
                .build();

        wishlistRepository.save(wishlist);
        log.info("위시리스트 추가 - userId: {}, productId: {}", user.getId(), productId);
    }

    /**
     *   위시리스트에서 상품 제거 - 수정
     */
    @Transactional
    public void removeFromWishlist(User user, Long productId) {
        Optional<Wishlist> wishlist = wishlistRepository.findByUserAndProductId(user, productId);

        //   위시리스트에 없어도 예외를 던지지 않고 로그만 남김
        if (wishlist.isEmpty()) {
            log.warn("위시리스트에서 제거 시도했으나 항목이 없음 - userId: {}, productId: {}", user.getId(), productId);
            return;
        }

        wishlistRepository.delete(wishlist.get());
        log.info("위시리스트 제거 - userId: {}, productId: {}", user.getId(), productId);
    }

    /**
     * 사용자의 위시리스트 조회
     */
    public List<ProductDTO> getUserWishlist(User user) {
        List<Wishlist> wishlists = wishlistRepository.findByUser(user);
        return wishlists.stream()
                .map(wishlist -> ProductDTO.fromEntity(wishlist.getProduct()))
                .collect(Collectors.toList());
    }

    /**
     * 특정 상품이 위시리스트에 있는지 확인
     */
    public boolean isInWishlist(User user, Long productId) {
        return wishlistRepository.existsByUserAndProductId(user, productId);
    }

    /**
     * 위시리스트 개수
     */
    public long getWishlistCount(User user) {
        return wishlistRepository.countByUser(user);
    }
}