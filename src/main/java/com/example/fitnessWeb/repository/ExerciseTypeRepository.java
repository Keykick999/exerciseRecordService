package com.example.fitnessWeb.repository;
import com.example.fitnessWeb.dto.ExerciseTypeProjection;
import com.example.fitnessWeb.entity.ExerciseType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


public interface ExerciseTypeRepository extends JpaRepository<ExerciseType, Long> {
    @Query("""
    SELECT e.id AS id, e.name AS name, m.name AS muscleGroup
    FROM ExerciseType e
    JOIN e.exerciseMuscles em
    JOIN em.muscle m
    WHERE (:muscleGroups IS NULL OR m.name IN :muscleGroups)
    """)
    Page<ExerciseTypeProjection> findByMuscleGroupsProjection(Set<String> muscleGroups, Pageable pageable);

    boolean existsByName(String name);

    @Query("SELECT e FROM ExerciseType e " +
            "LEFT JOIN FETCH e.exerciseMuscles em " +
            "LEFT JOIN FETCH em.muscle")
    List<ExerciseType> findAllWithMuscles();

    @Query("SELECT DISTINCT et FROM ExerciseType et " +
            "LEFT JOIN FETCH et.exerciseMuscles em " +
            "LEFT JOIN FETCH em.muscle " +
            "WHERE et.id IN :ids")
    List<ExerciseType> findWithMusclesByIdIn(@Param("ids") List<Long> ids);
}
