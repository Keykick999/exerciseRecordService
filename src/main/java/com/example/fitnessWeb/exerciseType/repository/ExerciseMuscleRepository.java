package com.example.fitnessWeb.exerciseType.repository;

import com.example.fitnessWeb.exerciseType.domain.ExerciseMuscle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseMuscleRepository extends JpaRepository<ExerciseMuscle, Long> {
}
