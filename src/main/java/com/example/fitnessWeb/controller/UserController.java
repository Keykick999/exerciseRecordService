package com.example.fitnessWeb.controller;

import com.example.fitnessWeb.dto.UserLoginRequest;
import com.example.fitnessWeb.dto.UserRegisterRequest;
import com.example.fitnessWeb.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody @Valid UserRegisterRequest dto) {
        userService.register(dto);
        return ResponseEntity.ok("회원가입 성공");
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody @Valid UserLoginRequest dto) {
        String token = userService.login(dto);
        return ResponseEntity.ok(token);
    }
}
