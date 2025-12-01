package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "inquiries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InquiryType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InquiryStatus status;

    @OneToMany(mappedBy = "inquiry", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<InquiryComment> comments = new ArrayList<>();

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // 비즈니스 메서드
    public void updateInquiry(String title, String content, InquiryType type) {
        this.title = title;
        this.content = content;
        this.type = type;
    }

    public void updateStatus(InquiryStatus status) {
        this.status = status;
    }

    public void addComment(InquiryComment comment) {
        this.comments.add(comment);
        comment.setInquiry(this);
        // 댓글이 추가되면 자동으로 답변완료 상태로 변경
        if (this.status == InquiryStatus.PENDING) {
            this.status = InquiryStatus.ANSWERED;
        }
    }

    // 문의 유형
    public enum InquiryType {
        PRODUCT("상품 문의"),
        ORDER("주문/배송"),
        RETURN("교환/환불"),
        CLAIM("불만/클레임"),
        ETC("기타");

        private final String description;

        public boolean selected = false;

        InquiryType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    // 문의 상태
    public enum InquiryStatus {
        PENDING("답변대기"),
        ANSWERED("답변완료"),
        CLOSED("처리완료");

        private final String description;

        InquiryStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}