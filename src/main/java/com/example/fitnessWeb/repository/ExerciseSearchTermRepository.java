package com.example.fitnessWeb.repository;

import com.example.fitnessWeb.entity.ExerciseSearchTerm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ExerciseSearchTermRepository extends JpaRepository<ExerciseSearchTerm, Long> {
    @Query("select e from ExerciseSearchTerm e where e.keyword = :keyword")
    List<ExerciseSearchTerm> findByKeyword(String keyword);
}
