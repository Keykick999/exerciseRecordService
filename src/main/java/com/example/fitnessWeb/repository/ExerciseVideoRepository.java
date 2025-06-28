package com.example.fitnessWeb.repository;

import com.example.fitnessWeb.entity.ExerciseVideo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseVideoRepository extends JpaRepository<ExerciseVideo, Long>, ExerciseVideoRepositoryCustom {
}
