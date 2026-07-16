package com.example.fitnessWeb.level.policy;

import com.example.fitnessWeb.user.entity.WorkoutGoal;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class ExperiencePolicyFactory {
    private final Map<WorkoutGoal, ExperiencePolicy> policies;

    public ExperiencePolicyFactory(List<ExperiencePolicy> polices) {
        EnumMap<WorkoutGoal, ExperiencePolicy> map = new EnumMap<>(WorkoutGoal.class);

        for (ExperiencePolicy policy : polices) {
            map.put(policy.getWorkoutGoal(), policy);
        }

        this.policies = Map.copyOf(map);
    }

    public ExperiencePolicy getExperiencePolicy(WorkoutGoal workoutGoal) {
        return policies.get(workoutGoal);
    }
}
