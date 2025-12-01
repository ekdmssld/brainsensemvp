package com.example.demo.controller;

import com.example.demo.dto.AddUserRequest;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequiredArgsConstructor
@Slf4j
public class UserController {

    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "signup", required = false) String signup,
            @RequestParam(value = "redirect", required = false) String redirect,
            Model model,
            HttpServletRequest request) {

        CsrfToken token = (CsrfToken) request.getAttribute("_csrf");

        model.addAttribute("csrfToken", token.getToken());
        model.addAttribute("csrfParameterName", token.getParameterName());

        if (error != null) {
            model.addAttribute("error", "아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        if (signup != null && signup.equals("success")) {
            model.addAttribute("signup", true);
        }

        if (redirect != null) {
            model.addAttribute("redirect", redirect);
        }

        return "user/login";
    }

    /**
     * 회원가입 페이지
     * GET /signup
     */
    @GetMapping("/signup")
    public String signupPage(HttpServletRequest request, Model model) {
        CsrfToken token = (CsrfToken) request.getAttribute("_csrf");

        model.addAttribute("csrfToken", token.getToken());
        model.addAttribute("csrfParameterName", token.getParameterName());

        return "user/signup";
    }


    /**
     * 회원가입 처리
     * POST /signup
     */
    @PostMapping("/signup")
    public String signup(AddUserRequest request, Model model) {
        log.info("회원가입 요청 - username: {}, email: {}", request.getUsername(), request.getEmail());

        try {
            // 중복 체크
            if (userRepository.findByUsername(request.getUsername()).isPresent()) {
                model.addAttribute("error", "이미 사용 중인 아이디입니다.");
                return "user/signup";
            }

            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                model.addAttribute("error", "이미 사용 중인 이메일입니다.");
                return "user/signup";
            }

            // 비밀번호 암호화
            String encodedPassword = bCryptPasswordEncoder.encode(request.getPassword());

            // ❌ 첫 번째 사용자 ADMIN 로직 제거
            // 일반 사용자로 생성
            User user = User.builder()
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .password(encodedPassword)
                    .phone(request.getPhone())
                    .address(request.getAddress())
                    .role(User.Role.USER)  // ✅ 기본은 USER
                    .build();

            userRepository.save(user);

            log.info("회원가입 성공 - username: {}, role: USER", request.getUsername());
            return "redirect:/login?signup=success";

        } catch (Exception e) {
            log.error("회원가입 실패", e);
            model.addAttribute("error", "회원가입 중 오류가 발생했습니다.");
            return "user/signup";
        }
    }
    @GetMapping("/test/encode-password")
    @ResponseBody
    public String testEncodePassword(@RequestParam String password) {
        return bCryptPasswordEncoder.encode(password);
    }
}
