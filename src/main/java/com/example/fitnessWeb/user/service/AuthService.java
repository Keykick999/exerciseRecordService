package com.example.fitnessWeb.user.service;

import com.example.fitnessWeb.email.service.EmailVerificationService;
import com.example.fitnessWeb.exception.DuplicateEmailException;
import com.example.fitnessWeb.exception.DuplicateUserIdException;
import com.example.fitnessWeb.exception.DuplicateUserNameException;
import com.example.fitnessWeb.exception.InvalidRefreshTokenException;
import com.example.fitnessWeb.user.dto.*;
import com.example.fitnessWeb.user.entity.User;
import com.example.fitnessWeb.user.jwt.JwtService;
import com.example.fitnessWeb.user.jwt.TokenStore;
import com.example.fitnessWeb.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authManager;
    private final JwtService jwt;
    private final TokenStore store;
    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;

    // 회원가입
    public String register(RegisterRequest req) {
        // userId 중복 확인
        if (users.existsByUserId(req.userId())) {
            throw new DuplicateUserIdException(req.userId());
        }

        // userName 중복 확인
        if (users.existsByUserName(req.userName())) {
            throw new DuplicateUserNameException(req.userName()); // 새로운 예외 클래스 정의 필요
        }

        // 이메일 중복 확인
        if (users.existsByEmail(req.email())) {
            throw new DuplicateEmailException(req.email()); // 새로운 예외 클래스 정의 필요
        }

        // 이메일 인증 선확인
        emailVerificationService.ensureVerified(req.email(), "signup");

        User u = new User();
        u.setUserId(req.userId());
        u.setPassword(passwordEncoder.encode(req.password()));
        u.setUserName(req.userName()); // username 저장
        u.setEmail(req.email());
        users.save(u);

        return u.getUserId();
    }



    // 로그인
    public TokenResponse login(LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password())
        );
        UserDetails user = (UserDetails) auth.getPrincipal();

        String access = jwt.generateAccessToken(user);
        String refresh = jwt.generateRefreshToken(user);

        long expMs = jwt.getExpiration(access).getTime() - System.currentTimeMillis();
        long refreshTtl = jwt.getExpiration(refresh).getTime() - System.currentTimeMillis();
        store.saveRefresh(user.getUsername(), refresh, refreshTtl);

        return new TokenResponse(access, expMs, refresh);
    }

    // 토큰 재발급
    public TokenResponse refresh(RefreshRequest req) {
        String refresh = req.refreshToken();
        if (!jwt.isTokenValid(refresh) || !jwt.isRefresh(refresh)) {
            throw new InvalidRefreshTokenException();
        }

        String username = jwt.getUsername(refresh);
        if (!store.isRefreshValid(username, refresh)) {
            throw new InvalidRefreshTokenException();
        }

        store.revokeRefresh(username, refresh);

        UserDetails stub = org.springframework.security.core.userdetails.User
                .withUsername(username).password("N/A").build();

        String newAccess = jwt.generateAccessToken(stub);
        String newRefresh = jwt.generateRefreshToken(stub);

        long expMs = jwt.getExpiration(newAccess).getTime() - System.currentTimeMillis();
        long refreshTtl = jwt.getExpiration(newRefresh).getTime() - System.currentTimeMillis();
        store.saveRefresh(username, newRefresh, refreshTtl);

        return new TokenResponse(newAccess, expMs, newRefresh);
    }

    public void logout(LogoutRequest req) {
        String refresh = req.refreshToken();
        if (!jwt.isTokenValid(refresh) || !jwt.isRefresh(refresh)) {
            throw new InvalidRefreshTokenException();
        }
        String username = jwt.getUsername(refresh);
        store.revokeRefresh(username, refresh);
    }

    public void logoutAll(String username) {
        store.revokeAll(username);
    }
}
