package com.example.fitnessWeb.exerciseType.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExerciseSearchDto {
    @NotBlank
    private Long id;

    @NotBlank
    private String name;
}
