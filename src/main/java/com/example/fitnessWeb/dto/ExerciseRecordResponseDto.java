package com.example.fitnessWeb.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class ExerciseRecordResponseDto {
    private LocalDate date;
    private String muscleGroup;
    private String exerciseName;
    private int setOrder;
    private double weight;
    private int reps;
}
