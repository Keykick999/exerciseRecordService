package com.example.fitnessWeb.exerciseType.repository;

import com.example.fitnessWeb.exerciseType.domain.ExerciseTypeDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.Set;

public interface ExerciseTypeSearchRepositoryCustom {
    Page<ExerciseTypeDocument> searchByNameAndMuscles(String name, Set<String> muscleGroups, Pageable pageable) throws IOException;
}
