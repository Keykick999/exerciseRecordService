package com.example.fitnessWeb.user.dto;

import com.example.fitnessWeb.user.service.LoginCommand;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(@NotBlank String username, @NotBlank String password) {
    public LoginCommand toCommand() {
        return new LoginCommand(username, password);
    }
}