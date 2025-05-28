package com.example.fitnessWeb.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ExerciseRecordResponseDto {
    private LocalDateTime createdAt;
    private String muscleGroup;
    private String exerciseName;
    private int setOrder;
    private double weight;
    private int reps;
}
