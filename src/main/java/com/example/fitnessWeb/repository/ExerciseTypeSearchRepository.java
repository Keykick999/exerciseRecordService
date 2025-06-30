package com.example.fitnessWeb.repository;

import com.example.fitnessWeb.entity.ExerciseTypeDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface ExerciseTypeSearchRepository extends ElasticsearchRepository<ExerciseTypeDocument, String> {
    List<ExerciseTypeDocument> findByNameContaining(String name); // 이름 검색
}
