package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="username", unique = true)
    private String username;

    @Column(unique = true)
    private String email;

    private String password;

    @Column(length = 20)
    private String phone;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.USER; // 사용자 권한 (USER, ADMIN)

    // 배송지 정보
    @Column(length = 200)
    private String address;

    @Column(updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now(); // 가입일

    // 권한 Enum
    public enum Role {
        USER("ROLE_USER"),
        ADMIN("ROLE_ADMIN");

        private final String value;

        Role(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    @Builder
    public User(String email, String password, String username){
        this.email = email;
        this.password = password;
        this.username = username;
        this.role = Role.USER;
        this.createdAt = LocalDateTime.now();
    }

    public User update(String username){
        this.username = username;
        return this;
    }

    public void updateAddress(String address, String phone) {
        this.address = address;
        this.phone = phone;
    }

    public void changeRole(Role role) {
        this.role = role;
    }

    @Override
    public String getPassword() {
        return password;
    }

    // ===== UserDetails 구현부 =====
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        //   ROLE_ 접두사 필수!
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return this.username; // username 대신 email 사용
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }
    // 프로필 수정
    public void updateProfile(String email, String phone, String address) {
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.updatedAt = LocalDateTime.now();
    }

    public void updatePassword(String newPassword) {
        this.password = newPassword;
        this.updatedAt = LocalDateTime.now();
    }
    // 생성일 포맷팅
    public String getFormattedCreatedAt() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return createdAt.format(formatter);
    }

    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }
    public boolean isUser() {
        return this.role == Role.USER;
    }
}