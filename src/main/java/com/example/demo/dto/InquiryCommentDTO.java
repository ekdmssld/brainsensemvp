package com.example.demo.dto;

import com.example.demo.entity.InquiryComment;
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
public class InquiryCommentDTO {

    private Long id;
    private Long inquiryId;
    private Long userId;
    private String username;
    private String content;
    private Boolean isAdmin;
    private LocalDateTime createdAt;
    private Boolean deletable;  //   추가

    public static InquiryCommentDTO fromEntity(InquiryComment comment) {
        return InquiryCommentDTO.builder()
                .id(comment.getId())
                .inquiryId(comment.getInquiry().getId())
                .userId(comment.getUser().getId())
                .username(comment.getUser().getUsername())
                .content(comment.getContent())
                .isAdmin(comment.getIsAdmin())
                .createdAt(comment.getCreatedAt())
                .deletable(false)  //   기본값
                .build();
    }

    public String getFormattedCreatedAt() {
        return createdAt != null ?
                createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "";
    }
}