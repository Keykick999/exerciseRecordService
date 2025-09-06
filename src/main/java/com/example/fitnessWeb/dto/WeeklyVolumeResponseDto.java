package com.example.fitnessWeb.dto;// package com.example.fitnessWeb.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyVolumeResponseDto {
    private Integer yearWeek;     // 예: 202535 (ISO 주차)
    private Double weeklyVolume;  // 주간 총 볼륨
}