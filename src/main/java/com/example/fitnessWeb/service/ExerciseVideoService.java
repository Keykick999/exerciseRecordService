package com.example.fitnessWeb.service;

import com.example.fitnessWeb.entity.ExerciseVideo;
import com.example.fitnessWeb.repository.ExerciseVideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ExerciseVideoService {

    private final ExerciseVideoRepository exerciseVideoRepository;

    public Page<ExerciseVideo> searchVideos(String muscleGroup, String athleteName, String category, Pageable pageable) {
        return exerciseVideoRepository.searchVideos(muscleGroup, athleteName, category, pageable);
    }
}
