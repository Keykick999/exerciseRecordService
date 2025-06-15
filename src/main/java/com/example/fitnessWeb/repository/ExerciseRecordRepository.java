package com.example.fitnessWeb.repository;

import com.example.fitnessWeb.entity.ExerciseRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExerciseRecordRepository extends JpaRepository<ExerciseRecord, Long>, ExerciseRecordRepositoryCustom {
}
