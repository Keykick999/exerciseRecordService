package com.example.fitnessWeb.exerciseVideo.repository;

import com.example.fitnessWeb.exerciseVideo.domain.ExerciseVideo;
import com.example.fitnessWeb.exerciseVideo.enums.ExerciseVideoType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface ExerciseVideoRepositoryCustom {
    Page<ExerciseVideo> searchVideos(List<String> muscleGroups, List<String> exerciseNames, List<String> athleteNames, ExerciseVideoType type, Pageable pageable);
}
