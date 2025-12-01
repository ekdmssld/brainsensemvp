package com.example.demo.dto;

import com.example.demo.entity.Inquiry;
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
public class InquiryDTO {

    private Long id;
    private Long userId;
    private String username;

    @Builder.Default
    private String title = "";

    @Builder.Default
    private String content = "";

    @Builder.Default
    private String type = "";

    private String typeDescription;
    private String status;
    private String statusDescription;
    private Long commentCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static InquiryDTO fromEntity(Inquiry inquiry) {
        return InquiryDTO.builder()
                .id(inquiry.getId())
                .userId(inquiry.getUser().getId())
                .username(inquiry.getUser().getUsername())
                .title(inquiry.getTitle())
                .content(inquiry.getContent())
                .type(inquiry.getType().name())
                .typeDescription(inquiry.getType().getDescription())
                .status(inquiry.getStatus().name())
                .statusDescription(inquiry.getStatus().getDescription())
                .commentCount((long) inquiry.getComments().size())
                .createdAt(inquiry.getCreatedAt())
                .updatedAt(inquiry.getUpdatedAt())
                .build();
    }

    public String getFormattedCreatedAt() {
        return createdAt != null ?
                createdAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "";
    }

    public String getFormattedUpdatedAt() {
        return updatedAt != null ?
                updatedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : "";
    }

    public String getStatusBadgeClass() {
        if (status == null) return "secondary";
        switch (status) {
            case "PENDING": return "warning";
            case "ANSWERED": return "primary";
            case "CLOSED": return "secondary";
            default: return "secondary";
        }
    }
}