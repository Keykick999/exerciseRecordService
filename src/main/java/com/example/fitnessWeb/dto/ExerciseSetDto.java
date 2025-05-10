package com.example.fitnessWeb.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExerciseSetDto {
    private int setOrder;
    private double weight;
    private int reps;
    private Long exerciseTypeId;
}