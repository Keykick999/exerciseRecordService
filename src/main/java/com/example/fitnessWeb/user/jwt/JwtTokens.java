package com.example.fitnessWeb.user.jwt;

public record JwtTokens(
        String accessToken,
        String refreshToken
) {}