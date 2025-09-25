package com.example.fitnessWeb.analytics.projection;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MaxVolumeSetRow {
    private Long recordId;
    private LocalDateTime createdAt;
    private Double volume;   // weight * reps
    private Double weight;
    private Integer reps;
}