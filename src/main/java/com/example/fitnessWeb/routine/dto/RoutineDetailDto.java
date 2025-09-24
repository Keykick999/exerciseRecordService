package com.example.fitnessWeb.routine.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RoutineDetailDto {
    private String name;
    private Long exerciseTypeId;
    private String exerciseType;
    private int setNumber;
    private double weight;
    private int repetitions;
}
