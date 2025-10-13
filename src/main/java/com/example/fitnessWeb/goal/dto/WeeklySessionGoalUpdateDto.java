package com.example.fitnessWeb.goal.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WeeklySessionGoalUpdateDto {
    private int achievedSessions;
    private boolean achieved;
}
