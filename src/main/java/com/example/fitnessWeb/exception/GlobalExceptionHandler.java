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

    @ExceptionHandler(RoutineNotFoundException.class)
    public ResponseEntity<String> handleRoutineNotFoundException(RoutineNotFoundException ex) {
        return ResponseEntity.status(404).body(ex.getMessage());
    }

    @ExceptionHandler(ChatNotFoundException.class)
    public ResponseEntity<String> handleChatNotFoundException(ChatNotFoundException ex) {
        return ResponseEntity.status(404).body(ex.getMessage());
    }

    @ExceptionHandler(DuplicateRoutineNameException.class)
    public ResponseEntity<String> handleDuplicateRoutineNameException(DuplicateRoutineNameException ex) {
        return ResponseEntity.status(409).body(ex.getMessage());
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public ResponseEntity<String> handleUnauthorizedAccessException(UnauthorizedAccessException ex) {
        return ResponseEntity.status(403).body(ex.getMessage());
    }

    @ExceptionHandler(EmptyExerciseSetListException.class)
    public ResponseEntity<String> handleEmptyExerciseSetListException(EmptyExerciseSetListException ex) {
        return ResponseEntity.status(400).body(ex.getMessage());
    }

    @ExceptionHandler(ExerciseRecordNotFoundException.class)
    public ResponseEntity<String> handleExerciseRecordNotFoundException(ExerciseRecordNotFoundException ex) {
        return ResponseEntity.status(404).body(ex.getMessage());
    }

    @ExceptionHandler(InvalidMuscleException.class)
    public ResponseEntity<String> handleInvalidMuscleException(InvalidMuscleException ex) {
        return ResponseEntity.status(400).body(ex.getMessage());
    }

    @ExceptionHandler(DuplicateExerciseTypeException.class)
    public ResponseEntity<String> handleDuplicateExerciseTypeException(DuplicateExerciseTypeException ex) {
        return ResponseEntity.status(404).body(ex.getMessage());
    }
}
