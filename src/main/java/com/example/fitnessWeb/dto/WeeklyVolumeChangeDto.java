package com.example.fitnessWeb.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class WeeklyVolumeChangeDto {
    private String username;
    private int yearWeek;
    private double weeklyVolume;
    private double volumeChangePct;  // 전주 대비 변화율(%)
}