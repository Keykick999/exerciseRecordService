package com.example.fitnessWeb.common.exception;

public class DuplicateUserIdException extends RuntimeException {
    public DuplicateUserIdException(String userId) {
        super("이미 사용 중인 userId: " + userId);
    }
}