package com.example.demo.controller;

import com.example.demo.dto.OrderDTO;
import com.example.demo.dto.ProductDTO;
import com.example.demo.entity.Category;
import com.example.demo.entity.User;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.CartService;
import com.example.demo.service.OrderService;
import com.example.demo.service.WishlistService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/mypage")
@RequiredArgsConstructor
@Slf4j
public class MyPageController {

    private final WishlistService wishlistService;
    private final CartService cartService;
    private final CategoryRepository categoryRepository;
    private final OrderService orderService;
    private final UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder;

    // 마이페이지 메인
    @GetMapping
    public String mypage(
            @AuthenticationPrincipal User user,
            Model model) {

        User refreshedUser = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        int orderCount = orderService.countOrdersByUsername(user.getUsername());

        List<Category> categories = categoryRepository.findByParentIsNull();
        long wishlistCount = wishlistService.getWishlistCount(refreshedUser);
        long cartCount = cartService.getCartCount(refreshedUser);

        model.addAttribute("user", refreshedUser);
        model.addAttribute("categories", categories);
        model.addAttribute("wishlistCount", wishlistCount);
        model.addAttribute("cartCount", cartCount);
        model.addAttribute("orderCount", orderCount);

        return "mypage/index";
    }

    // 마이페이지 - 위시리스트
    @GetMapping("/wishlist")
    public String mypageWishlist(
            @AuthenticationPrincipal User user,
            Model model) {

        List<ProductDTO> wishlistProducts = wishlistService.getUserWishlist(user);
        List<Category> categories = categoryRepository.findByParentIsNull();

        model.addAttribute("user", user);
        model.addAttribute("products", wishlistProducts);
        model.addAttribute("categories", categories);

        return "mypage/wishlist";
    }

    //주문 내역 확인
    @GetMapping("/orders")
    public String orders(@AuthenticationPrincipal User user, Model model) {

        List<OrderDTO> orders = orderService.getOrdersByUsername(user.getUsername());

        model.addAttribute("orders", orders);
        model.addAttribute("user", user);

        return "mypage/orders";
    }

    @GetMapping("/profile")
    public String myProfile(@AuthenticationPrincipal User user, Model model) {

        User refreshedUser = userRepository.findByUsername(user.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("사용자 정보를 찾을 수 없습니다."));

        model.addAttribute("user", refreshedUser);
        return "mypage/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(
            @AuthenticationPrincipal User user,
            @RequestParam String email,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String currentPassword,
            @RequestParam(required = false) String newPassword,
            HttpServletRequest request,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes) {

        try {
            // 사용자 정보 조회
            User userEntity = userRepository.findByUsername(user.getUsername())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

            boolean passwordChanged = false;

            // 비밀번호 변경 처리
            if (currentPassword != null && !currentPassword.isEmpty() &&
                    newPassword != null && !newPassword.isEmpty()) {

                // 현재 비밀번호 확인
                if (!passwordEncoder.matches(currentPassword, userEntity.getPassword())) {
                    redirectAttributes.addFlashAttribute("message", "현재 비밀번호가 일치하지 않습니다.");
                    redirectAttributes.addFlashAttribute("alertType", "danger");
                    return "redirect:/mypage/profile";
                }

                // 새 비밀번호로 변경
                userEntity.setPassword(passwordEncoder.encode(newPassword));
                passwordChanged = true;
                log.info("비밀번호 변경 - username: {}", user.getUsername());
            }

            // 기본 정보 업데이트
            userEntity.setEmail(email);
            userEntity.setPhone(phone);
            userEntity.setAddress(address);

            userRepository.save(userEntity);

            // ✅ 비밀번호 변경 시 로그아웃 후 로그인 페이지로 리다이렉트
            if (passwordChanged) {
                // 로그아웃 처리
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null) {
                    new SecurityContextLogoutHandler().logout(request, response, auth);
                }

                redirectAttributes.addFlashAttribute("message", "비밀번호가 변경되었습니다. 다시 로그인해주세요.");
                redirectAttributes.addFlashAttribute("alertType", "success");
                return "redirect:/login";
            }

            // 비밀번호 변경하지 않은 경우 마이페이지로 리다이렉트
            redirectAttributes.addFlashAttribute("message", "회원 정보가 수정되었습니다.");
            redirectAttributes.addFlashAttribute("alertType", "success");
            return "redirect:/mypage";

        } catch (Exception e) {
            log.error("회원 정보 수정 실패", e);
            redirectAttributes.addFlashAttribute("message", "회원 정보 수정 중 오류가 발생했습니다.");
            redirectAttributes.addFlashAttribute("alertType", "danger");
            return "redirect:/mypage/profile";
        }
    }
}