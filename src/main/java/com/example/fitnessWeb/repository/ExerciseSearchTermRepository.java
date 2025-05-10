package com.example.fitnessWeb.repository;

import com.example.fitnessWeb.entity.ExerciseSearchTerm;
import com.example.fitnessWeb.entity.ExerciseType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExerciseSearchTermRepository extends JpaRepository<ExerciseSearchTerm, Long> {
    @Query("select e from ExerciseSearchTerm e where e.keyword = :keyword")
    List<ExerciseSearchTerm> findByKeyword(String keyword);

    @Query("select distinct e.exerciseType from ExerciseSearchTerm e join e.exerciseType where e.keyword = :keyword")
    List<ExerciseType> findExerciseTypesByKeyword(@Param("keyword") String keyword);
}
