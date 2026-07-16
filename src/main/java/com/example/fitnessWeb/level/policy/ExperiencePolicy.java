package com.example.fitnessWeb.level.policy;

import com.example.fitnessWeb.user.entity.WorkoutGoal;
import com.example.fitnessWeb.level.dto.ExerciseSetCommand;
import java.util.List;

public interface ExperiencePolicy {
    WorkoutGoal getWorkoutGoal();
    int calculateXp(List<ExerciseSetCommand> exercises);
}