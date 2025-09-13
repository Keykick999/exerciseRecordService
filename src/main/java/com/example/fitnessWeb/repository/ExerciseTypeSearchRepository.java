package com.example.fitnessWeb.repository;

import com.example.fitnessWeb.entity.ExerciseType;
import com.example.fitnessWeb.entity.ExerciseTypeDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.jpa.repository.Query;

public interface ExerciseTypeSearchRepository extends ElasticsearchRepository<ExerciseTypeDocument, String>, ExerciseTypeSearchRepositoryCustom {
}
