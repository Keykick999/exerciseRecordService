package com.example.fitnessWeb.exerciseRecord.dto;

import com.example.fitnessWeb.exerciseRecord.service.SaveExerciseSetCommand;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ExerciseSetDto {
    private Long exerciseTypeId;
    private int setOrder;
    private double weight;
    private int reps;

    public SaveExerciseSetCommand toCommand() {
        return new SaveExerciseSetCommand(
                exerciseTypeId,
                setOrder,
                weight,
                reps
        );
    }
}