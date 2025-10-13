package com.example.fitnessWeb.goal.dto;

import com.example.fitnessWeb.goal.entity.ExerciseGoal;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ExerciseGoalResponseDto {
    private Long id;
    private String exerciseTypeName;
    private double weight;
    private int reps;
    private boolean achieved;

    public static ExerciseGoalResponseDto fromEntity(ExerciseGoal goal) {
        return ExerciseGoalResponseDto.builder()
                .id(goal.getId())
                .exerciseTypeName(goal.getExerciseType().getName())
                .weight(goal.getWeight())
                .reps(goal.getReps())
                .achieved(goal.isAchieved())
                .build();
    }
}
