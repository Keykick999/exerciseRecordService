package com.example.fitnessWeb.user.service;

import com.example.fitnessWeb.common.exception.*;
import com.example.fitnessWeb.email.EmailVerificationPurpose;
import com.example.fitnessWeb.email.service.EmailVerificationService;
import com.example.fitnessWeb.user.dto.TokenResponse;
import com.example.fitnessWeb.user.entity.User;
import com.example.fitnessWeb.user.jwt.JwtService;
import com.example.fitnessWeb.user.jwt.JwtTokens;
import com.example.fitnessWeb.user.jwt.TokenStore;
import com.example.fitnessWeb.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static org.springframework.security.core.userdetails.User.withUsername;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final TokenStore store;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;

    /**
     * 회원가입
     */
    public void register(RegisterCommand command) {
        checkDuplicateUserId(command.userId());
        checkDuplicateUserName(command.userName());
        checkDuplicateEmail(command.email());
        emailVerificationService.ensureVerified(command.email(), EmailVerificationPurpose.SIGNUP);

        String encodedPassword = passwordEncoder.encode(command.password());
        User u = User.createUser(command, encodedPassword);
        userRepository.save(u);
    }

    /**
     * 로그인
     */
    public TokenResponse login(LoginCommand command) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(command.username(), command.password())
        );
        UserDetails user = (UserDetails) auth.getPrincipal();

        return generateTokens(user);
    }

    /**
     * 토큰 재발급
     */
    public TokenResponse refresh(RefreshCommand command) {
        String refreshToken = command.refreshToken();
        jwtService.validateRefreshToken(command.refreshToken());

        String username = jwtService.getUsername(refreshToken);
        if (!store.isRefreshTokenValid(username, refreshToken)) {
            throw new InvalidRefreshTokenException();
        }

        store.revokeRefreshToken(username, refreshToken);
        UserDetails stub = withUsername(username).password("N/A").build();

        return generateTokens(stub);
    }

    /**
     * 로그아웃
     */
    public void logout(LogoutCommand command) {
        String refresh = command.refreshToken();
        validateRefreshToken(refresh);

        String username = jwtService.getUsername(refresh);
        store.revokeRefreshToken(username, refresh);
    }

    /**
     * 아이디 중복 체크
     */
    public void checkDuplicateUserId(String userId) {
        if (userRepository.existsByUserId(userId)) {
            throw new DuplicateUserIdException(userId);
        }
    }

    /**
     * 회원명 중복 체크
     */
    public void checkDuplicateUserName(String userName) {
        if (userRepository.existsByUserName(userName)) {
            throw new DuplicateUserNameException(userName);
        }
    }

    // ========================= 공용 재사용 메서드 =========================

    public User findUserOrThrow(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
    }

    public User getUserReference(String userId) {
        return userRepository.getReferenceById(userId);
    }

    // ========================= private 헬퍼 메서드 =========================
    private TokenResponse generateTokens(UserDetails user) {
        JwtTokens jwtTokens = jwtService.generateTokens(user);

        long accessExpiresIn = jwtService.getExpiresIn(jwtTokens.accessToken());
        long refreshTtl = jwtService.getExpiresIn(jwtTokens.refreshToken());


        store.saveRefreshToken(user.getUsername(), jwtTokens.refreshToken(), refreshTtl);

        return new TokenResponse(jwtTokens.accessToken(), accessExpiresIn, jwtTokens.refreshToken());
    }

    private void validateRefreshToken(String refresh) {
        if (!jwtService.isTokenValid(refresh) || !jwtService.isRefresh(refresh)) {
            throw new InvalidRefreshTokenException();
        }
    }

    private void checkDuplicateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException(email);
        }
    }
}
