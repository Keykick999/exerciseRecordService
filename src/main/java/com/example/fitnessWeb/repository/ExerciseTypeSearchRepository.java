package com.example.fitnessWeb.repository;

import com.example.fitnessWeb.entity.ExerciseTypeDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ExerciseTypeSearchRepository extends ElasticsearchRepository<ExerciseTypeDocument, String>, ExerciseTypeSearchRepositoryCustom {
}
