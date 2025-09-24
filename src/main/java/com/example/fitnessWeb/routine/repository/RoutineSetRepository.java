package com.example.fitnessWeb.routine.repository;

import com.example.fitnessWeb.routine.domain.RoutineSet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoutineSetRepository extends JpaRepository<RoutineSet, Long> {
    List<RoutineSet> findByRoutineId(Long routineId);
}
