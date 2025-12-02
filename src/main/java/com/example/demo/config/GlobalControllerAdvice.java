package com.example.demo.config;

import com.example.demo.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class GlobalControllerAdvice {

    @ModelAttribute("_csrf")
    public CsrfToken csrfToken(HttpServletRequest request) {
        return (CsrfToken) request.getAttribute("_csrf");
    }

    @ModelAttribute("isAdmin")
    public boolean addIsAdmin(@AuthenticationPrincipal User user) {
        if (user == null) return false;
        return user.isAdmin();  // 여기서 위에서 만든 isAdmin() 사용
    }

    @ModelAttribute("loginUser")
    public User addLoginUser(@AuthenticationPrincipal User user) {
        return user; // 헤더에서 유저 정보도 접근 가능하도록
    }
}
