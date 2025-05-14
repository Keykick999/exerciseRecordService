package com.example.fitnessWeb.repository;

import com.example.fitnessWeb.entity.RoutineSet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoutineSetRepository extends JpaRepository<RoutineSet, Long> {
    List<RoutineSet> findByRoutineId(Long routineId);
}
