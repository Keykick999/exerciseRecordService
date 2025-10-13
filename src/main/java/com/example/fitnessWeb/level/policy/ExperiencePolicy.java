package com.example.fitnessWeb.level.policy;

import com.example.fitnessWeb.exerciseRecord.dto.ExerciseSetDto;

import java.util.List;

public interface ExperiencePolicy {
    int calculateXp(List<ExerciseSetDto> exercises);
}