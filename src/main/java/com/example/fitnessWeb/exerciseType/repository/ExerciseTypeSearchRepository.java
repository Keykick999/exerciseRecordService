package com.example.fitnessWeb.exerciseType.repository;

import com.example.fitnessWeb.exerciseType.domain.ExerciseTypeDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ExerciseTypeSearchRepository extends ElasticsearchRepository<ExerciseTypeDocument, String>, ExerciseTypeSearchRepositoryCustom {
}
