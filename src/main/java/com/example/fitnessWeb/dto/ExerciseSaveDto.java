package com.example.fitnessWeb.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExerciseSaveDto {
    private String name;  // 예: Bench Press

    private String muscleGroup; // 예: Chest, Back, Legs 등
}
