package com.example.fitnessWeb.goal.repository;

import com.example.fitnessWeb.goal.entity.WeeklySessionGoal;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WeeklySessionGoalRepository extends JpaRepository<WeeklySessionGoal, Long> {
    List<WeeklySessionGoal> findByUser_UserId(String userId);
}
