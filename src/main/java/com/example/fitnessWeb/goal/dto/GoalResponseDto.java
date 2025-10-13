package com.example.fitnessWeb.goal.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GoalResponseDto {
    private Long id;
    private String goalType;
    private Object details;
}
