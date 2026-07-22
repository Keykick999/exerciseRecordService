package com.example.fitnessWeb.analytics.dto;

import java.util.List;

public record ExerciseTypeVolumeDto(
        Long exerciseTypeId,
        double totalVolume, // 기간 내 전체 볼륨
        List<WeightVolumeDto> weightVolumes
) {

    public record WeightVolumeDto(
            double weight,
            double percentage // 전체 대비 퍼센트
    ) {
    }
}
