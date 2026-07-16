package com.example.fitnessWeb.user.service;

public record LoginCommand(String username, String password) {
    @Override
    public String toString() {
        return "LoginCommand[username=" + username + ", password=****]";
    }
}