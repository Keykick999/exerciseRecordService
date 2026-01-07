package com.example.fitnessWeb.user.controller;

import com.example.fitnessWeb.user.dto.*;
import com.example.fitnessWeb.user.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService auth;

    @GetMapping("/check-id")
    public ResponseEntity<Void> checkId(@RequestParam String userId) {
        auth.checkDuplicateUserId(userId); // 중복이면 예외 발생, 아니면 200 OK
        return ResponseEntity.ok().build();
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterRequest req) {
        String userId = auth.register(req);
        return ResponseEntity.created(java.net.URI.create("/api/users/" + userId)).build();
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody @Valid LoginRequest req) {
        return ResponseEntity.ok(auth.login(req));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody @Valid RefreshRequest req) {
        return ResponseEntity.ok(auth.refresh(req));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody @Valid LogoutRequest req) {
        auth.logout(req);
        return ResponseEntity.noContent().build();
    }
}