package com.example.fitnessWeb.level.policy;

import com.example.fitnessWeb.exerciseRecord.dto.ExerciseSetDto;

import java.util.List;

public class CountBasedExperience implements ExperiencePolicy {
    @Override
    public int calculateXp(List<ExerciseSetDto> exercises) {
        return exercises.stream()
                .mapToInt(ExerciseSetDto::getReps)
                .sum();
    }
}