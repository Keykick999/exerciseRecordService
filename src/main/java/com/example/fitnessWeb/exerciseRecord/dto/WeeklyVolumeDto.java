package com.example.fitnessWeb.exerciseRecord.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WeeklyVolumeDto {
    private Integer yearWeek;       // YEARWEEK 값
    private Double weeklyVolume; // 합산 볼륨 (weight * reps)
}
