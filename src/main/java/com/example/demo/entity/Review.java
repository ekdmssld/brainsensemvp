package com.example.demo.entity;

import com.example.demo.entity.Product;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private User member; // 작성자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product; // 상품

    @Column(nullable = false)
    private Integer rating; // 평점 (1~5)

    @Column(columnDefinition = "TEXT")
    private String content; // 리뷰 내용

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ReviewComment> comments = new ArrayList<>(); // 리뷰 댓글들

    @Column(updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now(); // 작성일

    @Column
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now(); // 수정일

    // 비즈니스 메서드
    public void updateReview(Integer rating, String content) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("평점은 1~5 사이여야 합니다.");
        }
        this.rating = rating;
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    public void addComment(ReviewComment comment) {
        this.comments.add(comment);
        comment.setReview(this);
    }

    public static Review createReview(User member, Product product, Integer rating, String content) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("평점은 1~5 사이여야 합니다.");
        }
        return Review.builder()
                .member(member)
                .product(product)
                .rating(rating)
                .content(content)
                .build();
    }
}