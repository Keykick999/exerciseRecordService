package com.example.fitnessWeb.goal.repository;

import com.example.fitnessWeb.goal.entity.ExerciseGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ExerciseGoalRepository extends JpaRepository<ExerciseGoal, Long> {
    List<ExerciseGoal> findByUser_UserId(String userId);
}
