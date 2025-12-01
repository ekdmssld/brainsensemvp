package com.example.demo.service;

import com.example.demo.dto.InquiryCommentDTO;
import com.example.demo.dto.InquiryDTO;
import com.example.demo.entity.Inquiry;
import com.example.demo.entity.InquiryComment;
import com.example.demo.entity.User;
import com.example.demo.repository.InquiryCommentRepository;
import com.example.demo.repository.InquiryRepository;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InquiryService {

    private final InquiryRepository inquiryRepository;
    private final InquiryCommentRepository commentRepository;
    private final UserRepository userRepository;

    // 문의 목록 조회 (사용자)
    public Page<InquiryDTO> getUserInquiries(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        return inquiryRepository.findByUser(user, pageable)
                .map(InquiryDTO::fromEntity);
    }

    // 문의 목록 조회 (전체 - 관리자용)
    public Page<InquiryDTO> getAllInquiries(Pageable pageable) {
        return inquiryRepository.findAll(pageable)
                .map(InquiryDTO::fromEntity);
    }

    // 문의 검색
    public Page<InquiryDTO> searchInquiries(String keyword, Pageable pageable) {
        return inquiryRepository.searchByKeyword(keyword, pageable)
                .map(InquiryDTO::fromEntity);
    }

    // 문의 상세 조회
    public InquiryDTO getInquiryById(Long id) {
        Inquiry inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("문의를 찾을 수 없습니다."));

        return InquiryDTO.fromEntity(inquiry);
    }

    // 문의 상세 조회 (댓글 포함)
    public InquiryDTO getInquiryWithComments(Long id) {
        Inquiry inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("문의를 찾을 수 없습니다."));

        return InquiryDTO.fromEntity(inquiry);
    }

    // 문의 등록
    @Transactional
    public Inquiry createInquiry(String username, String title, String content, String type) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        Inquiry inquiry = Inquiry.builder()
                .user(user)
                .title(title)
                .content(content)
                .type(Inquiry.InquiryType.valueOf(type))
                .status(Inquiry.InquiryStatus.PENDING)
                .build();

        Inquiry saved = inquiryRepository.save(inquiry);
        log.info("문의 등록 완료 - ID: {}, 작성자: {}", saved.getId(), username);

        return saved;
    }

    // 문의 수정
    @Transactional
    public Inquiry updateInquiry(Long id, String title, String content, String type, String username) {
        Inquiry inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("문의를 찾을 수 없습니다."));

        // 작성자 본인인지 확인
        if (!inquiry.getUser().getUsername().equals(username)) {
            throw new RuntimeException("본인의 문의만 수정할 수 있습니다.");
        }

        // 답변이 달린 경우 수정 불가
        if (inquiry.getStatus() != Inquiry.InquiryStatus.PENDING) {
            throw new RuntimeException("답변이 달린 문의는 수정할 수 없습니다.");
        }

        inquiry.updateInquiry(title, content, Inquiry.InquiryType.valueOf(type));

        Inquiry updated = inquiryRepository.save(inquiry);
        log.info("문의 수정 완료 - ID: {}", id);

        return updated;
    }

    // 문의 삭제
    @Transactional
    public void deleteInquiry(Long id, String username) {
        Inquiry inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("문의를 찾을 수 없습니다."));

        // 작성자 본인인지 확인
        if (!inquiry.getUser().getUsername().equals(username)) {
            throw new RuntimeException("본인의 문의만 삭제할 수 있습니다.");
        }

        inquiryRepository.delete(inquiry);
        log.info("문의 삭제 완료 - ID: {}", id);
    }

    // 댓글 목록 조회
    public List<InquiryCommentDTO> getComments(Long inquiryId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new RuntimeException("문의를 찾을 수 없습니다."));

        return commentRepository.findByInquiryOrderByCreatedAtAsc(inquiry)
                .stream()
                .map(InquiryCommentDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // 댓글 등록
    @Transactional
    public InquiryComment createComment(Long inquiryId, String username, String content) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new RuntimeException("문의를 찾을 수 없습니다."));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // 관리자인지 확인
        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        InquiryComment comment = InquiryComment.builder()
                .inquiry(inquiry)
                .user(user)
                .content(content)
                .isAdmin(isAdmin)
                .build();

        inquiry.addComment(comment);

        InquiryComment saved = commentRepository.save(comment);
        log.info("댓글 등록 완료 - 문의 ID: {}, 작성자: {}", inquiryId, username);

        return saved;
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(Long commentId, String username) {
        InquiryComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

        // 작성자 본인 또는 관리자만 삭제 가능
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        boolean isAdmin = user.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        if (!comment.getUser().getUsername().equals(username) && !isAdmin) {
            throw new RuntimeException("댓글을 삭제할 권한이 없습니다.");
        }

        commentRepository.delete(comment);
        log.info("댓글 삭제 완료 - ID: {}", commentId);
    }

    // 문의 상태 변경 (관리자)
    @Transactional
    public void updateInquiryStatus(Long inquiryId, String status) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new RuntimeException("문의를 찾을 수 없습니다."));

        inquiry.updateStatus(Inquiry.InquiryStatus.valueOf(status));
        inquiryRepository.save(inquiry);
        log.info("문의 상태 변경 - ID: {}, 상태: {}", inquiryId, status);
    }

    // 답변 대기 중인 문의 개수
    public long getPendingInquiryCount() {
        return inquiryRepository.countByStatus(Inquiry.InquiryStatus.PENDING);
    }
}