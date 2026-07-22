package com.example.fitnessWeb.analytics.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * @param username    ← userId 대신 username
 * @param start       ISO yyyy-MM-dd
 * @param end         ISO yyyy-MM-dd
 * @param totalVolume 전체 볼륨 합
 */
public record MuscleBalanceResponse(String username, LocalDate start, LocalDate end, Double totalVolume, List<Item> byMuscle) {

    /**
     * @param pct 0~100, 소수1자리
     */
    public record Item(String muscle, Double volume, Double totalVolume, Double pct) {
    }
}