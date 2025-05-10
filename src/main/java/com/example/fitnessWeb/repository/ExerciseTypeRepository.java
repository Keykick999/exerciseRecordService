package com.example.fitnessWeb.repository;
import com.example.fitnessWeb.entity.ExerciseType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


public interface ExerciseTypeRepository extends JpaRepository<ExerciseType, Long> {
    @Query("select e from ExerciseType e where e.name like concat('%', :name, '%')")
    Page<ExerciseType> findByName(String name, Pageable pageable);

    @Query("select e from ExerciseType e where e.muscleGroup in :muscleGroups")
    Page<ExerciseType> findByMuscleGroups(HashSet<String> muscleGroups, Pageable pageable);

    @Query("select e from ExerciseType e where e.muscleGroup in :muscleGroups " +
            "and e.name like concat('%', :name, '%')")
    Page<ExerciseType> findByNameAndMuscleGroup(String name, List<String> muscleGroups, Pageable pageable);

//    Page<ExerciseType> findByMuscleGroupContaining(String muscleGroup, Pageable pageable);

    boolean existsByName(String name);

    @Query("select e from ExerciseType e where e.id in :ids")
    Page<ExerciseType> findByIds(Set<Long> ids, Pageable pageable);

    @Query("select e from ExerciseType e where e.id in :ids " +
            "and e.muscleGroup in :muscleGroups")
    Page<ExerciseType> findByIdsAndMuscleGroups(Set<Long> ids, HashSet<String> muscleGroups, Pageable pageable);
}
