package com.example.fitnessWeb.repository;

import com.example.fitnessWeb.entity.ExerciseVideo;
import com.example.fitnessWeb.entity.ExerciseVideoType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ExerciseVideoRepositoryCustom {
    Page<ExerciseVideo> searchVideos(List<String> muscleGroups, List<String> exerciseNames, List<String> athleteNames, ExerciseVideoType type, Pageable pageable);
}
