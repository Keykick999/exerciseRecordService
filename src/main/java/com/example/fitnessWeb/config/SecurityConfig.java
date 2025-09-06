package com.example.fitnessWeb.config;

import com.example.fitnessWeb.user.jwt.JwtAuthenticationFilter;
import com.example.fitnessWeb.user.jwt.JwtService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
//                .formLogin(AbstractHttpConfigurer::disable)
//                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(reg -> reg
                        // permitAll 목록 (두 번째 코드 기준)
                        .requestMatchers(
                                "/api/users/**",
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
                                "/speech",
                                "/chat",
                                "/workoutVideos"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/exercise-types").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/exercise-types/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/analytics/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/exercise-types/sync-to-es").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/email").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/email/verify").permitAll()

                        // 나머지는 인증 필요
                        .anyRequest().authenticated()
                );

        http.addFilterBefore(new JwtAuthenticationFilter(jwt, uds), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
