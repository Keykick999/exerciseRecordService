package com.example.fitnessWeb.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(401).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidRoutineSetException.class)
    public ResponseEntity<String> handleInvalidRoutineSet(InvalidRoutineSetException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(DuplicateRoutineSetException.class)
    public ResponseEntity<String> handleDuplicateRoutineSetException(DuplicateRoutineSetException ex) {
        return ResponseEntity.badRequest().body(ex.getMessage());
    }

    @ExceptionHandler(ExerciseTypeNotFoundException.class)
    public ResponseEntity<String> handleExerciseTypeNotFound(ExerciseTypeNotFoundException ex) {
        return ResponseEntity.status(404).body(ex.getMessage());
    }
}
