package com.example.demo.service;

import com.example.demo.dto.CartDTO;
import com.example.demo.entity.Cart;
import com.example.demo.entity.Product;
import com.example.demo.entity.User;
import com.example.demo.repository.CartRepository;
import com.example.demo.repository.ProductRepository;
import com.example.demo.repository.UserRepository;
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
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    // username으로 User 찾기
    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + username));
    }

    // 장바구니 아이템 조회
    public List<CartDTO> getCartItems(String username) {
        User user = findUserByUsername(username);
        List<Cart> carts = cartRepository.findByMember(user);  // ✅ findByMember

        return carts.stream()
                .map(CartDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // 장바구니에 상품 추가
    public void addToCart(String username, Long productId, Integer quantity) {
        User user = findUserByUsername(username);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        // 이미 장바구니에 있는지 확인
        Optional<Cart> existingCart = cartRepository.findByMemberAndProduct(user, product);  // ✅ findByMemberAndProduct

        if (existingCart.isPresent()) {
            // 수량만 증가
            Cart cart = existingCart.get();
            cart.addQuantity(quantity);
            cartRepository.save(cart);
        } else {
            // 새로 추가
            Cart newCart = Cart.createCart(user, product, quantity);
            cartRepository.save(newCart);
        }

        log.info("장바구니 추가 - username: {}, productId: {}, quantity: {}",
                username, productId, quantity);
    }

    // 장바구니 아이템 수량 변경
    public void updateQuantity(String username, Long cartId, Integer quantity) {
        User user = findUserByUsername(username);
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니 아이템을 찾을 수 없습니다."));

        if (!cart.getMember().getId().equals(user.getId())) {  // ✅ getMember
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        cart.changeQuantity(quantity);
        cartRepository.save(cart);
    }

    // 장바구니 아이템 삭제
    public void removeFromCart(String username, Long cartId) {
        User user = findUserByUsername(username);
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니 아이템을 찾을 수 없습니다."));

        if (!cart.getMember().getId().equals(user.getId())) {  // ✅ getMember
            throw new IllegalArgumentException("권한이 없습니다.");
        }

        cartRepository.delete(cart);
        log.info("장바구니 삭제 - cartId: {}", cartId);
    }

    // 장바구니 전체 삭제
    public void clearCart(String username) {
        User user = findUserByUsername(username);
        cartRepository.deleteByMember(user);  // ✅ deleteByMember
        log.info("장바구니 전체 삭제 - username: {}", username);
    }

    // 장바구니 총 금액
    public Integer getTotalPrice(String username) {
        User user = findUserByUsername(username);
        List<Cart> carts = cartRepository.findByMember(user);  // ✅ findByMember
        return carts.stream()
                .mapToInt(Cart::getTotalPrice)
                .sum();
    }

    // 장바구니 개수
    public long getCartCount(User user) {
        return cartRepository.countByMember(user);  // ✅ countByMember
    }
}