package com.example.fitnessWeb.exerciseRecord.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExerciseSetDetailDto {
    private String name;
    private int setOrder;
    private double weight;
    private int reps;
}
