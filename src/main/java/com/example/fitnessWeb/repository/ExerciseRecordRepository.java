package com.example.fitnessWeb.repository;

import com.example.fitnessWeb.entity.ExerciseRecord;
import com.example.fitnessWeb.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ExerciseRecordRepository extends JpaRepository<ExerciseRecord, Long> {
    List<ExerciseRecord> findByUserAndDate(User user, LocalDate date);

    List<ExerciseRecord> findByUserAndDateAfter(User user, LocalDate dateAfter);
}