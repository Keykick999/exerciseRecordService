package com.example.fitnessWeb.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ExerciseTypeResponseDto {
    private Long id;
    private String name;
    private List<String> muscleGroups;
}
