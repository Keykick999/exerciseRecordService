package com.example.fitnessWeb.repository;

import com.example.fitnessWeb.entity.Routine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoutineRepository extends JpaRepository<Routine, Long> {
}
