package com.example.fitnessWeb.repository;

import com.example.fitnessWeb.entity.Muscle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MuscleRepository extends JpaRepository<Muscle, Long> {
    List<Muscle> findAllByNameIn(List<String> names);
}
