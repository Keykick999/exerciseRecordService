package com.example.fitnessWeb.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class NutritionRecordUpdateRequest {

    @NotNull(message = "consumedAt(섭취 시각)은 필수입니다.")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime consumedAt;

    @NotNull @DecimalMin(value = "0.0") @DecimalMax(value = "1000.0")
    private BigDecimal proteinG;

    @NotNull @DecimalMin(value = "0.0") @DecimalMax(value = "1000.0")
    private BigDecimal carbsG;

    @NotNull @DecimalMin(value = "0.0") @DecimalMax(value = "1000.0")
    private BigDecimal fatG;

    @NotNull @DecimalMin(value = "0.0") @DecimalMax(value = "20000.0")
    private BigDecimal caloriesKcal;

    @NotNull
    private Boolean dailyTotal;

    public LocalDateTime getConsumedAt() { return consumedAt; }
    public void setConsumedAt(LocalDateTime consumedAt) { this.consumedAt = consumedAt; }
    public BigDecimal getProteinG() { return proteinG; }
    public void setProteinG(BigDecimal proteinG) { this.proteinG = proteinG; }
    public BigDecimal getCarbsG() { return carbsG; }
    public void setCarbsG(BigDecimal carbsG) { this.carbsG = carbsG; }
    public BigDecimal getFatG() { return fatG; }
    public void setFatG(BigDecimal fatG) { this.fatG = fatG; }
    public BigDecimal getCaloriesKcal() { return caloriesKcal; }
    public void setCaloriesKcal(BigDecimal caloriesKcal) { this.caloriesKcal = caloriesKcal; }
    public Boolean getDailyTotal() { return dailyTotal; }
    public void setDailyTotal(Boolean dailyTotal) { this.dailyTotal = dailyTotal; }
}
