package com.example.demo.entity;

import com.example.demo.entity.Review;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "review_comments")
public class ReviewComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private Review review; // 리뷰

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private User member; // 작성자

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content; // 댓글 내용

    @Column(updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now(); // 작성일

    @Column
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now(); // 수정일

    // 비즈니스 메서드
    public void setReview(Review review) {
        this.review = review;
    }

    public void updateContent(String content) {
        this.content = content;
        this.updatedAt = LocalDateTime.now();
    }

    public static ReviewComment createComment(Review review, User member, String content) {
        return ReviewComment.builder()
                .review(review)
                .member(member)
                .content(content)
                .build();
    }
}