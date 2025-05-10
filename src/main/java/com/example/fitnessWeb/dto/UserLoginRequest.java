package com.example.fitnessWeb.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class UserLoginRequest {
    @NotBlank
    private String username;

    @NotBlank
    private String password;
}