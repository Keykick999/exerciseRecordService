package com.example.fitnessWeb.level.policy;

import com.example.fitnessWeb.user.entity.WorkoutGoal;
import com.example.fitnessWeb.level.dto.ExerciseSetCommand;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class VolumeBasedExperience implements ExperiencePolicy {
    @Override
    public WorkoutGoal getWorkoutGoal() {
        return WorkoutGoal.HYPERTROPHY;
    }

    @Override
    public int calculateXp(List<ExerciseSetCommand> exercises) {
        return exercises.stream()
                .mapToInt(e -> (int)(e.weight() * e.reps()))
                .sum();
    }
}