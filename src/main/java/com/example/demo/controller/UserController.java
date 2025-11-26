package com.example.demo.controller;

import com.example.demo.dto.AddUserRequest;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@Slf4j
public class UserController {

    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "redirect", required = false) String redirect,
                            Model model) {
        log.info("로그인 페이지 접근");

        if (error != null) {
            model.addAttribute("error", "아이디 또는 비밀번호가 올바르지 않습니다.");
        }

        if (redirect != null) {
            model.addAttribute("redirect", redirect);
        }
        model.addAttribute("keyword", "");
        return "user/login";
    }

    /**
     * 회원가입 페이지
     * GET /signup
     */
    @GetMapping("/signup")
    public String signupPage() {
        log.info("회원가입 페이지 접근");
        return "user/signup";
    }

    /**
     * 회원가입 처리
     * POST /signup
     */
    @PostMapping("/signup")
    public String signup(AddUserRequest request, Model model) {
        log.info("회원가입 요청 - username: {}", request.getUsername());

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

            // 사용자 생성
            User user = User.builder()
                    .username(request.getUsername())
                    .email(request.getEmail())
                    .password(encodedPassword)
                    .phone(request.getPhone())
                    .address(request.getAddress())
                    .role(User.Role.USER)
                    .build();

            userRepository.save(user);

            log.info("회원가입 성공 - username: {}", request.getUsername());
            return "redirect:/login?signup=success";

        } catch (Exception e) {
            log.error("회원가입 실패", e);
            model.addAttribute("error", "회원가입 중 오류가 발생했습니다.");
            return "user/signup";
        }
    }
}
