package com.example.demo.dto;

import com.example.demo.entity.ReviewComment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewCommentDTO {
    private Long id;
    private Long reviewId;
    private Long memberId;
    private String memberName;
    private Long productId;          //    추가: 댓글에서도 productId 접근
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // UI용 필드
    private String formattedCreatedAt;
    private boolean deletable;       // 삭제 가능 여부 (작성자 / 관리자)

    public static ReviewCommentDTO fromEntity(ReviewComment comment) {
        return ReviewCommentDTO.builder()
                .id(comment.getId())
                .reviewId(comment.getReview().getId())
                .memberId(comment.getMember().getId())
                .memberName(comment.getMember().getUsername())
                .productId(comment.getReview().getProduct().getId())   //    여기서 세팅
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .formattedCreatedAt(comment.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .deletable(false)   // 기본값, 서비스에서 나중에 true/false로 바꿈
                .build();
    }
}