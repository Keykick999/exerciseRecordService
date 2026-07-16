package com.example.fitnessWeb.user.controller;

import com.example.fitnessWeb.user.dto.*;
import com.example.fitnessWeb.user.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest req) {
        authService.register(req.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid LoginRequest req) {
        return ResponseEntity.ok(authService.login(req.toCommand()));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody @Valid RefreshRequest req) {
        return ResponseEntity.ok(authService.refresh(req.toCommand()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody @Valid LogoutRequest req) {
        authService.logout(req.toCommand());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/check-id")
    public ResponseEntity<Void> checkId(@RequestParam String userId) {
        authService.checkDuplicateUserId(userId); // 중복이면 예외 발생, 아니면 200 OK
        return ResponseEntity.ok().build();
    }

    @GetMapping("/check-username")
    public ResponseEntity<Void> checkUserName(@RequestParam String userName) {
        authService.checkDuplicateUserName(userName); // 중복이면 예외 발생, 아니면 200 OK
        return ResponseEntity.ok().build();
    }
}