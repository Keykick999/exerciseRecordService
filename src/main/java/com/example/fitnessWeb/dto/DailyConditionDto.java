package com.example.fitnessWeb.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class DailyConditionDto {

    @NotNull
    private LocalDate date;

    @Positive
    private double weight;

    @Positive
    private double muscleMass;

    @Min(1) @Max(10)
    private int conditionLevel;

    private String note;
}
