package com.example.fitnessWeb.user.service;

public record LogoutCommand(String refreshToken) {
    @Override
    public String toString() {
        return "LogoutCommand[refreshToken=****]";
    }
}
