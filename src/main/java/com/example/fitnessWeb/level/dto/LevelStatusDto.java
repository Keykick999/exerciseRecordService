package com.example.fitnessWeb.level.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LevelStatusDto {
    private int level;          // 현재 레벨
    private int currentXp;      // 누적 경험치
    private int remainingXp;    // 다음 레벨까지 남은 경험치
    private double progress;    // 현재 레벨 진행률(%)
}