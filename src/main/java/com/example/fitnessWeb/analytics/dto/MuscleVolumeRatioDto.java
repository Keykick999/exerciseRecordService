package com.example.fitnessWeb.analytics.dto;

public record MuscleVolumeRatioDto(
        String muscle,
        Double volume,
        Double totalVolume,
        Double pct
) {}