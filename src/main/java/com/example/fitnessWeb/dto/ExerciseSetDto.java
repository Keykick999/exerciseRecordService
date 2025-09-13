package com.example.fitnessWeb.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ExerciseSetDto {
    private Long exerciseTypeId;
    private int setOrder;
    private double weight;
    private int reps;
}