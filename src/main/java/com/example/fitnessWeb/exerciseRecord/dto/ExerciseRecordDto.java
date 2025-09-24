package com.example.fitnessWeb.exerciseRecord.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ExerciseRecordDto {
    private List<ExerciseSetDto> sets;
}