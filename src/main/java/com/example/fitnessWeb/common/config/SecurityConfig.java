package com.example.fitnessWeb.common.config;

import com.example.fitnessWeb.user.jwt.JwtAuthenticationFilter;
import com.example.fitnessWeb.user.jwt.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    // 비밀번호 인코딩
    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) throws Exception {
        return cfg.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtService jwt, UserDetailsService uds) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendRedirect("/login"))
                )
                .authorizeHttpRequests(reg -> reg
                        // 1. 로그인 및 회원가입 API만 허용
                        .requestMatchers("/api/users/login", "/api/users/register", "/api/users/check-id/**", "/api/users//check-username/**", "/api/email/**")
                        .permitAll()

                        // 2. View: ViewController에 설정된 모든 경로 허용
                        .requestMatchers(
                                "/",
                                "/login",
                                "/routine",
                                "/routines",
                                "/chat",
                                "/exercise-records",
                                "/exercise-record-detail/**",
                                "/routineChat",
                                "/speech",
                                "/workoutVideos",
                                "/analysis"
                        ).permitAll()

                        // 3. Static Resources: CSS, JS, Images 등 정적 파일 허용
                        .requestMatchers(
                                "/favicon.ico",
                                "/error",        // 에러 페이지 접근 시 리다이렉트 방지
                                "/css/**",
                                "/js/**",
                                "/images/**"
                        ).permitAll()

                        // 4. 나머지는 전부 인증 필요
                        .anyRequest().authenticated()
                );

        http.addFilterBefore(new JwtAuthenticationFilter(jwt, uds), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}


