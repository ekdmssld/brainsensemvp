package com.example.demo.dto;

import com.example.demo.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDTO {

    private Long id;
    private Long memberId;
    private String memberName;
    private Long productId;
    private String productName;
    private Integer rating;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<ReviewCommentDTO> comments;

    // UI용 필드
    private String formattedCreatedAt;
    private String formattedUpdatedAt;
    private boolean editable;

    private static String safe(String v) {
        return (v == null || v.trim().isEmpty()) ? null : v;
    }

    public static ReviewDTO fromEntity(Review review) {

        LocalDateTime created = review.getCreatedAt();
        LocalDateTime updated = review.getUpdatedAt();

        return ReviewDTO.builder()
                .id(review.getId())
                .memberId(review.getMember().getId())
                .memberName(safe(review.getMember().getUsername()))
                .productId(review.getProduct().getId())
                .productName(safe(review.getProduct().getName()))
                .rating(review.getRating())
                .content(safe(review.getContent()))
                .createdAt(created)
                .updatedAt(updated)
                .formattedCreatedAt(created != null ?
                        created.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "")
                .formattedUpdatedAt(updated != null ?
                        updated.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "")
                .comments(review.getComments().stream()
                        .map(ReviewCommentDTO::fromEntity)
                        .collect(Collectors.toList()))
                .build();
    }

    public String getStarRating() {
        return "★".repeat(rating) + "☆".repeat(5 - rating);
    }
}