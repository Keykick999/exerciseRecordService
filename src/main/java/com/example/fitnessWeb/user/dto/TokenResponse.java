package com.example.fitnessWeb.user.dto;

public record TokenResponse(String accessToken, long expiresInMs, String refreshToken) {}