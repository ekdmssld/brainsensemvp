package com.example.demo.repository;

import com.example.demo.entity.Review;
import com.example.demo.entity.ReviewComment;
import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewCommentRepository extends JpaRepository<ReviewComment, Long> {
    // 리뷰의 모든 댓글 조회 (작성일 오름차순)
    List<ReviewComment> findByReviewOrderByCreatedAtAsc(Review review);

    // 리뷰의 댓글 개수
    long countByReview(Review review);

    // 사용자가 작성한 모든 댓글
    List<ReviewComment> findByMemberOrderByCreatedAtDesc(User member);
}