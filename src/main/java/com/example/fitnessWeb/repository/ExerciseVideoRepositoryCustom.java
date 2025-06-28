package com.example.fitnessWeb.repository;

import com.example.fitnessWeb.entity.ExerciseVideo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ExerciseVideoRepositoryCustom {
    Page<ExerciseVideo> searchVideos(String muscleGroup, String athleteName, String category, Pageable pageable);
}
