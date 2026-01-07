package com.example.fitnessWeb.email.controller;

import com.example.fitnessWeb.email.dto.SendCodeRequest;
import com.example.fitnessWeb.email.dto.VerifyCodeRequest;
import com.example.fitnessWeb.email.service.EmailVerificationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
public class EmailVerificationController {

    private final EmailVerificationService service;

    public EmailVerificationController(EmailVerificationService service) {
        this.service = service;
    }

    @GetMapping("/check-email")
    public ResponseEntity<Void> checkEmail(@RequestParam String email) {
        service.checkEmailDuplicate(email);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<String> send(@RequestBody @Valid SendCodeRequest req) {
        service.sendVerificationCode(req.email(), req.purpose());
        return ResponseEntity.ok("인증 코드가 전송되었습니다.");
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verify(@RequestBody @Valid VerifyCodeRequest req) {
        service.verifyAndMarkAsVerified(req.email(), req.purpose(), req.code());
        return ResponseEntity.ok("인증 코드가 확인되었습니다.");
    }
}