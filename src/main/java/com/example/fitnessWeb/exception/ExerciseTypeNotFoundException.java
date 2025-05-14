package com.example.fitnessWeb.exception;

public class ExerciseTypeNotFoundException extends RuntimeException {
    public ExerciseTypeNotFoundException(Long id) {
        super("ID " + id + "에 해당하는 운동 종목이 존재하지 않습니다.");
    }
}
