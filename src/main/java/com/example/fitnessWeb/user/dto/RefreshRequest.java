package com.example.fitnessWeb.user.dto;

import com.example.fitnessWeb.user.service.RefreshCommand;
import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(@NotBlank String refreshToken) {
    public RefreshCommand toCommand() {
        return new RefreshCommand(refreshToken);
    }
}