package com.example.demo.repository;

import com.example.demo.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // username으로 사용자 조회
    Optional<User> findByUsername(String username);

    // email로 사용자 조회
    Optional<User> findByEmail(String email);

    // username 존재 여부 확인
    boolean existsByUsername(String username);

    // email 존재 여부 확인
    boolean existsByEmail(String email);

    Page<User> findByUsernameContainingOrEmailContaining(
            String username, String email, Pageable pageable);
}