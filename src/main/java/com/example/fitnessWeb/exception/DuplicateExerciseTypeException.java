package com.example.fitnessWeb.exception;

public class DuplicateExerciseTypeException extends RuntimeException{
    public DuplicateExerciseTypeException(String message) {
        super(message);
    }
}
