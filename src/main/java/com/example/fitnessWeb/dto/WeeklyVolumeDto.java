package com.example.fitnessWeb.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WeeklyVolumeDto {
    private String username;
    private int yearWeek;
    private double weeklyVolume;
}
