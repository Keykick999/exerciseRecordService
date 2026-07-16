package com.example.fitnessWeb.user.service;

public record RefreshCommand(String refreshToken) {
    @Override
    public String toString() {
        return "RefreshCommand{" +
                "refreshToken='****'" +
                '}';
    }
}
