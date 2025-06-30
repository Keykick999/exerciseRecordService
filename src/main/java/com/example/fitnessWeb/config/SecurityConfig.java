package com.example.fitnessWeb.config;

import com.example.fitnessWeb.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())  // ✅ 폼 로그인 제거
                .httpBasic(httpBasic -> httpBasic.disable())  // ✅ Basic 인증 제거
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/users/**",
                                "/home",
                                "/login",
                                "/routine",
                                "/routines",
                                "/exercise-records",
                                "/exercise-record-detail/**",
                                "/routine-detail.html",
                                "/exercise-session.html",
                                "/api/exercise-types/sync-to-es",
                                "/images/**",
                                "/css/**",
                                "/js/**",
                                "/favicon.ico",
                                "/routineChat",
                                "speech",
                                "chat",
                                "workoutVideos").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/exercise-types").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtUtil, customUserDetailsService),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
