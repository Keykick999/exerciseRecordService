package com.example.fitnessWeb.common.exception;

public class ChatNotFoundException extends RuntimeException{

    public ChatNotFoundException(String message) {
        super(message);
    }
}