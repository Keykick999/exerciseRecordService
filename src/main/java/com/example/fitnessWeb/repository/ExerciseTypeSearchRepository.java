package com.example.fitnessWeb.repository;

import com.example.fitnessWeb.entity.ExerciseType;
import com.example.fitnessWeb.entity.ExerciseTypeDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.data.jpa.repository.Query;

public interface ExerciseTypeSearchRepository extends ElasticsearchRepository<ExerciseTypeDocument, String>, ExerciseTypeSearchRepositoryCustom {
    @Query(value = "SELECT DISTINCT et FROM ExerciseType et LEFT JOIN FETCH et.exerciseMuscles",
            countQuery = "SELECT COUNT(et) FROM ExerciseType et")
    Page<ExerciseType> findAllWithMusclesUsingFetchJoin(Pageable pageable);
}
