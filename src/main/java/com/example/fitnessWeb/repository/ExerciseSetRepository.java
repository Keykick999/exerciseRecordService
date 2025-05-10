package com.example.fitnessWeb.repository;

import com.example.fitnessWeb.entity.ExerciseSet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseSetRepository extends JpaRepository<ExerciseSet, Long> {}