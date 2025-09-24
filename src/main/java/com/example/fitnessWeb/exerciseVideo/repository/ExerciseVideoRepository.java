package com.example.fitnessWeb.exerciseVideo.repository;

import com.example.fitnessWeb.exerciseVideo.domain.ExerciseVideo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseVideoRepository extends JpaRepository<ExerciseVideo, Long>, ExerciseVideoRepositoryCustom {
}
