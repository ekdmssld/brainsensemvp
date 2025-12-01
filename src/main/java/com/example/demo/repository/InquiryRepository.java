package com.example.demo.repository;

import com.example.demo.entity.Inquiry;
import com.example.demo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    // 사용자별 문의 조회
    Page<Inquiry> findByUser(User user, Pageable pageable);

    // 상태별 문의 조회
    Page<Inquiry> findByStatus(Inquiry.InquiryStatus status, Pageable pageable);

    // 유형별 문의 조회
    Page<Inquiry> findByType(Inquiry.InquiryType type, Pageable pageable);

    // 제목 또는 내용으로 검색
    @Query("SELECT i FROM Inquiry i WHERE i.title LIKE %:keyword% OR i.content LIKE %:keyword%")
    Page<Inquiry> searchByKeyword(String keyword, Pageable pageable);

    // 사용자의 문의 개수
    long countByUser(User user);

    // 답변 대기 중인 문의 개수
    long countByStatus(Inquiry.InquiryStatus status);
}