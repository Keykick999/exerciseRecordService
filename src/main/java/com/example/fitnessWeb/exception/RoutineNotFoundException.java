package com.example.fitnessWeb.exception;

public class RoutineNotFoundException extends RuntimeException {
    public RoutineNotFoundException() {
        super("등록된 루틴이 없습니다.");
    }

    public RoutineNotFoundException(String message) {
        super(message);
    }
}
