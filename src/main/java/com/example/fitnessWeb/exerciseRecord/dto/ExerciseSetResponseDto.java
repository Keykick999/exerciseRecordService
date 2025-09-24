package com.example.fitnessWeb.exerciseRecord.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExerciseSetResponseDto {
    private int setOrder;
    private double weight;
    private int reps;
}