package com.example.fitnessWeb.exerciseVideo.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseVideoDetailResponse {
    private String muscleGroup;
    private String exerciseName;
    private String athleteName;
}