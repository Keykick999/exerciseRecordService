package com.example.fitnessWeb.level.policy;

import com.example.fitnessWeb.exerciseRecord.dto.ExerciseSetDto;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class VolumeBasedExperience implements ExperiencePolicy {
    @Override
    public int calculateXp(List<ExerciseSetDto> exercises) {
        return exercises.stream()
                .mapToInt(e -> (int)(e.getWeight() * e.getReps()))
                .sum();
    }
}