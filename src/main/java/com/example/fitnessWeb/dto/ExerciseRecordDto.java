package com.example.fitnessWeb.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class ExerciseRecordDto {
    private LocalDate date;
    private List<ExerciseSetDto> sets;
}