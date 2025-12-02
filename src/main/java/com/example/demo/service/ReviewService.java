package com.example.demo.service;

import com.example.demo.dto.ReviewDTO;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewCommentRepository reviewCommentRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    // 상품의 모든 리뷰 조회
    public List<ReviewDTO> getProductReviews(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        return reviewRepository.findByProductOrderByCreatedAtDesc(product).stream()
                .map(ReviewDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // 리뷰 작성 권한 확인 (배송 완료된 상품만)
    public boolean canWriteReview(String username, Long productId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        // 이미 리뷰를 작성했는지 확인
        if (reviewRepository.existsByMemberAndProduct(user, product)) {
            return false;
        }

        // 배송 완료된 주문이 있는지 확인
        return orderRepository.findByMember(user).stream()
                .anyMatch(order ->
                        order.getStatus() == Order.OrderStatus.DELIVERED &&
                                order.getOrderItems().stream()
                                        .anyMatch(item -> item.getProduct().getId().equals(productId))
                );
    }

    // 리뷰 작성
    @Transactional
    public ReviewDTO createReview(String username, Long productId, Integer rating, String content) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        // 리뷰 작성 권한 확인
        if (!canWriteReview(username, productId)) {
            throw new IllegalStateException("리뷰 작성 권한이 없습니다.");
        }

        Review review = Review.createReview(user, product, rating, content);
        reviewRepository.save(review);

        log.info("리뷰 작성 - username: {}, productId: {}, rating: {}", username, productId, rating);
        return ReviewDTO.fromEntity(review);
    }

    // 리뷰 수정
    @Transactional
    public ReviewDTO updateReview(Long reviewId, String username, Integer rating, String content) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

        // 작성자 본인 확인
        if (!review.getMember().getUsername().equals(username)) {
            throw new IllegalStateException("본인의 리뷰만 수정할 수 있습니다.");
        }

        review.updateReview(rating, content);
        log.info("리뷰 수정 - reviewId: {}, username: {}", reviewId, username);

        return ReviewDTO.fromEntity(review);
    }

    // 리뷰 삭제
    @Transactional
    public void deleteReview(Long reviewId, String username) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

        // 작성자 본인 확인
        if (!review.getMember().getUsername().equals(username)) {
            throw new IllegalStateException("본인의 리뷰만 삭제할 수 있습니다.");
        }

        reviewRepository.delete(review);
        log.info("리뷰 삭제 - reviewId: {}, username: {}", reviewId, username);
    }

    // 리뷰 댓글 작성
    @Transactional
    public void createComment(Long reviewId, String username, String content) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        ReviewComment comment = ReviewComment.createComment(review, user, content);
        reviewCommentRepository.save(comment);

        log.info("리뷰 댓글 작성 - reviewId: {}, username: {}", reviewId, username);
    }

    // 리뷰 댓글 삭제
    @Transactional
    public void deleteComment(Long commentId, String username) {
        ReviewComment comment = reviewCommentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        // 작성자 본인 확인
        if (!comment.getMember().getUsername().equals(username)) {
            throw new IllegalStateException("본인의 댓글만 삭제할 수 있습니다.");
        }

        reviewCommentRepository.delete(comment);
        log.info("리뷰 댓글 삭제 - commentId: {}, username: {}", commentId, username);
    }

    // 상품 평균 평점 계산
    public Double getAverageRating(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        List<Review> reviews = reviewRepository.findByProductOrderByCreatedAtDesc(product);

        if (reviews.isEmpty()) {
            return 0.0;
        }

        return reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }

    // 리뷰 개수 조회
    public long getReviewCount(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("상품을 찾을 수 없습니다."));

        return reviewRepository.countByProduct(product);
    }
}