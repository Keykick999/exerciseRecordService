package com.example.fitnessWeb.goal.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseGoalRequestDto {
    private Long exerciseTypeId; // 클라이언트에서 전달
    private double weight;
    private int reps;
}
