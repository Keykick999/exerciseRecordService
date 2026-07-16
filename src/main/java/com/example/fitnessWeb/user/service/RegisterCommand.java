package com.example.fitnessWeb.user.service;

public record RegisterCommand(String userId, String password, String userName, String email) {
    @Override
    public String toString() {
        return "RegisterCommand{userName='" + userName + "'}";
    }
}