package com.example.fitnessWeb.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ExerciseTypeVolumeDto {

    private Long exerciseTypeId;
    private double totalVolume; // 기간 내 전체 볼륨
    private List<WeightVolumeDto> weightVolumes;

    @Getter
    @AllArgsConstructor
    public static class WeightVolumeDto {
        private double weight;
        private double percentage; // 전체 대비 퍼센트
    }
}
