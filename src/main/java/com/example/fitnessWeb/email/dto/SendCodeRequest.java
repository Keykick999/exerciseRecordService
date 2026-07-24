package com.example.fitnessWeb.email.dto;

import com.example.fitnessWeb.email.EmailVerificationPurpose;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SendCodeRequest(
        @Email @NotBlank String email,
        @NotBlank EmailVerificationPurpose purpose
) {}