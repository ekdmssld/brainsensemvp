package com.example.demo.repository;

import com.example.demo.entity.Inquiry;
import com.example.demo.entity.InquiryComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InquiryCommentRepository extends JpaRepository<InquiryComment, Long> {

    // 문의별 댓글 조회
    List<InquiryComment> findByInquiryOrderByCreatedAtAsc(Inquiry inquiry);

    // 문의별 댓글 개수
    long countByInquiry(Inquiry inquiry);
}