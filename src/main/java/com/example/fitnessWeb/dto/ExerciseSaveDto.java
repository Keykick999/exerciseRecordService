package com.example.fitnessWeb.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;

@Getter
@Setter
public class ExerciseSaveDto {
    private String name;  // 예: Bench Press

    private List<String> muscleGroups; // 예: Chest, Back, Legs 등
}
