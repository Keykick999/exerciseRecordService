package com.example.fitnessWeb.exerciseType.repository;

import com.example.fitnessWeb.exerciseType.domain.Muscle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface MuscleRepository extends JpaRepository<Muscle, Long> {
    List<Muscle> findAllByNameIn(List<String> names);
    Optional<Muscle> findByName(String name);
}
