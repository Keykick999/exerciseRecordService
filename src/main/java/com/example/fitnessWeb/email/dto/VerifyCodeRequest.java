package com.example.fitnessWeb.email.dto;

import com.example.fitnessWeb.email.EmailVerificationPurpose;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyCodeRequest(
        @Email @NotBlank String email,
        @NotBlank EmailVerificationPurpose purpose,
        @NotBlank @Pattern(regexp = "\\d{6}") String code
) {}
