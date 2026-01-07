package com.example.fitnessWeb.user.service;

import com.example.fitnessWeb.common.exception.*;
import com.example.fitnessWeb.email.service.EmailVerificationService;
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
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;

    // ========================= 핵심 기능 (Controller에서 호출) =========================

    /**
     * 회원가입
     */
    public String register(RegisterRequest req) {
        checkDuplicateUserId(req.userId());
        checkDuplicateUserName(req.userName());
        checkDuplicateEmail(req.email());
        emailVerificationService.ensureVerified(req.email(), "signup");

        User u = createUser(req);
        userRepository.save(u);

        return u.getUserId();
    }

    /**
     * 로그인
     */
    public TokenResponse login(LoginRequest req) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username(), req.password())
        );
        UserDetails user = (UserDetails) auth.getPrincipal();

        return generateTokens(user);
    }

    /**
     * 토큰 재발급
     */
    public TokenResponse refresh(RefreshRequest req) {
        String refresh = req.refreshToken();
        validateRefreshToken(refresh);

        String username = jwt.getUsername(refresh);
        if (!store.isRefreshValid(username, refresh)) {
            throw new InvalidRefreshTokenException();
        }

        store.revokeRefresh(username, refresh);
        UserDetails stub = org.springframework.security.core.userdetails.User
                .withUsername(username).password("N/A").build();

        return generateTokens(stub);
    }

    /**
     * 로그아웃
     */
    public void logout(LogoutRequest req) {
        String refresh = req.refreshToken();
        validateRefreshToken(refresh);

        String username = jwt.getUsername(refresh);
        store.revokeRefresh(username, refresh);
    }

    // ========================= 공용 재사용 메서드 =========================

    public User findUserOrThrow(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
    }

    // ========================= private 헬퍼 메서드 =========================

    private User createUser(RegisterRequest req) {
        User u = new User();
        u.setUserId(req.userId());
        u.setPassword(passwordEncoder.encode(req.password()));
        u.setUserName(req.userName());
        u.setEmail(req.email());
        u.setXp(0);
        u.setLevel(1);
        return u;
    }

    private TokenResponse generateTokens(UserDetails user) {
        String access = jwt.generateAccessToken(user);
        String refresh = jwt.generateRefreshToken(user);

        long expMs = jwt.getExpiration(access).getTime() - System.currentTimeMillis();
        long refreshTtl = jwt.getExpiration(refresh).getTime() - System.currentTimeMillis();
        store.saveRefresh(user.getUsername(), refresh, refreshTtl);

        return new TokenResponse(access, expMs, refresh);
    }

    private void validateRefreshToken(String refresh) {
        if (!jwt.isTokenValid(refresh) || !jwt.isRefresh(refresh)) {
            throw new InvalidRefreshTokenException();
        }
    }

    public void checkDuplicateUserId(String userId) {
        if (userRepository.existsByUserId(userId)) {
            throw new DuplicateUserIdException(userId);
        }
    }

    public void checkDuplicateUserName(String userName) {
        if (userRepository.existsByUserName(userName)) {
            throw new DuplicateUserNameException(userName);
        }
    }

    // ========================= private 단일 책임 헬퍼 (검증) =========================
    private void checkDuplicateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException(email);
        }
    }
}
