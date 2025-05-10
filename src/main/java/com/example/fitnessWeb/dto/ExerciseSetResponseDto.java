package com.example.fitnessWeb.dto;

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