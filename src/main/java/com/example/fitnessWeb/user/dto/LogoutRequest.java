package com.example.fitnessWeb.user.dto;

import com.example.fitnessWeb.user.service.LogoutCommand;
import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(@NotBlank String refreshToken) {
    public LogoutCommand toCommand() {
        return new LogoutCommand(refreshToken);
    }
}