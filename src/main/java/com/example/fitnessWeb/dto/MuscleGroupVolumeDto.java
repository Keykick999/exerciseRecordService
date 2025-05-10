package com.example.fitnessWeb.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MuscleGroupVolumeDto {
    private String muscleGroup;
    private double volume;
}