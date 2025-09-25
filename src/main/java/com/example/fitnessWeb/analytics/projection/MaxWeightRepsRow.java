package com.example.fitnessWeb.analytics.projection;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class MaxWeightRepsRow {
    private Long id;
    private LocalDateTime createdAt;
    private Double weight;
    private Integer reps;
}
