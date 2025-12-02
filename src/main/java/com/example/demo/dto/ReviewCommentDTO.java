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
    private boolean deletable;

    private static String safe(String value) {
        return (value == null || value.trim().isEmpty()) ? null : value;
    }

    public static ReviewCommentDTO fromEntity(ReviewComment comment) {
        String safeName = comment.getMember().getUsername();
        if (safeName == null || safeName.isBlank()) {
            safeName = "관리자";
        }

        return ReviewCommentDTO.builder()
                .id(comment.getId())
                .reviewId(comment.getReview().getId())
                .memberId(comment.getMember().getId())
                .memberName(safeName)
                .content(comment.getContent() == null ? "" : comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .formattedCreatedAt(
                        comment.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                )
                .build();
    }
}