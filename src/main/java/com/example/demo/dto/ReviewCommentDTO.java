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
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // UI용 필드
    private String formattedCreatedAt;
    private boolean deletable; // 삭제 가능 여부

    public static ReviewCommentDTO fromEntity(ReviewComment comment) {
        return ReviewCommentDTO.builder()
                .id(comment.getId())
                .reviewId(comment.getReview().getId())
                .memberId(comment.getMember().getId())
                .memberName(comment.getMember().getUsername())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .formattedCreatedAt(comment.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                .build();
    }
}