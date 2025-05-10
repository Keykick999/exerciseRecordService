package com.example.fitnessWeb.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@Builder
public class WeeklyMuscleStatDto {
    private LocalDate weekStart;
    private List<MuscleGroupVolumeDto> muscleStats;
}