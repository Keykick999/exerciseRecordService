package com.example.fitnessWeb.user.jwt;

public interface TokenStore {
    void saveRefreshToken(String username, String refreshToken, long ttlMs);
    boolean isRefreshTokenValid(String username, String refreshToken);
    void revokeRefreshToken(String username, String refreshToken);
}