package com.example.fitnessWeb.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class NutritionEntryCreateRequest {
    @NotNull private LocalDateTime consumedAt;

    @NotNull private BigDecimal proteinG;
    @NotNull private BigDecimal carbsG;
    @NotNull private BigDecimal fatG;
    @NotNull private BigDecimal caloriesKcal;
}