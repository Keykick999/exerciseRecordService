package com.example.fitnessWeb.common.exception;

public class RoutineNotFoundException extends RuntimeException {
    public RoutineNotFoundException() {
        super("등록된 루틴이 없습니다.");
    }

    public RoutineNotFoundException(String message) {
        super(message);
    }
}
