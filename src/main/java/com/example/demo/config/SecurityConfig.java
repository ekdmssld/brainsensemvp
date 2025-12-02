package com.example.demo.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/h2-console/**").permitAll()
//                        .requestMatchers("/", "/products/**", "/login", "/signup", "/css/**", "/js/**", "/uploads/**").permitAll()
//                        .requestMatchers("/test/**").permitAll()
//
//                        // 로그인 필요
//                        .requestMatchers("/cart/**", "/order/**", "/mypage/**", "/wishlist/**").authenticated()
//                        .requestMatchers("/admin/**").hasRole("ADMIN")
//                        .anyRequest().permitAll()
//                )
//
//                // 로그인 설정
//                .formLogin(form -> form
//                        .loginPage("/login")
//                        .loginProcessingUrl("/login")
//                        .defaultSuccessUrl("/", true)
//                        .failureUrl("/login?error=true")
//                        .usernameParameter("username")
//                        .passwordParameter("password")
//                        .permitAll()
//                )
//
//                // 로그아웃 GET 허용
//                .logout(logout -> logout
//                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET"))
//                        .logoutSuccessUrl("/")
//                        .permitAll()
//                )
//
//                // 비로그인 접근 시 팝업 + /login 이동
//                .exceptionHandling(ex -> ex
//                        .authenticationEntryPoint(new CustomAuthEntryPoint())
//                )
//
//                .csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
//
//                .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()));
//
//        return http.build();
//    }
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers("/h2-console/**").permitAll()
                    .requestMatchers("/", "/products/**", "/login", "/signup", "/css/**", "/js/**", "/uploads/**").permitAll()
                    .requestMatchers("/test/**").permitAll()
                    .requestMatchers("/cart/**", "/order/**", "/mypage/**", "/wishlist/**").authenticated()
                    .requestMatchers("/admin/**").hasRole("ADMIN")
                    .anyRequest().permitAll()
            )
            .formLogin(form -> form
                    .loginPage("/login")
                    .loginProcessingUrl("/login")
                    .defaultSuccessUrl("/", true)
                    .failureUrl("/login?error=true")
                    .usernameParameter("username")
                    .passwordParameter("password")
                    .permitAll()
            )
            .logout(logout -> logout
                    .logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET")) // ✅ 여기만 변경
                    .logoutSuccessUrl("/")
                    .permitAll()
            )
            .csrf(csrf -> csrf
                    .ignoringRequestMatchers("/h2-console/**")
                    .ignoringRequestMatchers("/cart/**", "/wishlist/**", "/order/**")
                    .ignoringRequestMatchers("/admin/**")
                    .ignoringRequestMatchers("/inquiry/**")
                    .ignoringRequestMatchers("/mypage/**")
            )
            .headers(headers -> headers
                    .frameOptions(frameOptions -> frameOptions.disable())
            );

    return http.build();
}

    @Component
    public class CustomAuthEntryPoint implements AuthenticationEntryPoint {
        @Override
        public void commence(HttpServletRequest request,
                             HttpServletResponse response,
                             AuthenticationException authException) throws IOException {

            response.setContentType("text/html;charset=UTF-8");
            response.getWriter().println(
                    "<script>alert('로그인이 필요합니다.'); location.href='/login';</script>"
            );
        }
    }
}
