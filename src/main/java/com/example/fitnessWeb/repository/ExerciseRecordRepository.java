package com.example.fitnessWeb.repository;

import com.example.fitnessWeb.dto.ExerciseRecordResponseDto;
import com.example.fitnessWeb.entity.ExerciseRecord;
import com.example.fitnessWeb.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ExerciseRecordRepository extends JpaRepository<ExerciseRecord, Long> {
    List<ExerciseRecord> findByUserAndDate(User user, LocalDate date);

    List<ExerciseRecord> findByUserAndDateAfter(User user, LocalDate dateAfter);

    @Query("""
        SELECT new com.example.fitnessWeb.dto.ExerciseRecordResponseDto(
            r.date,
            t.muscleGroup,
            t.name,
            s.setOrder,
            s.weight,
            s.reps
        )
        FROM ExerciseRecord r
        JOIN r.sets s
        JOIN s.exerciseType t
        WHERE r.user.id = :userId
          AND (:startDate IS NULL OR r.date >= :startDate)
          AND (:endDate IS NULL OR r.date <= :endDate)
          AND (:muscleGroup IS NULL OR t.muscleGroup = :muscleGroup)
          AND (:exerciseName IS NULL OR t.name like concat('%', :exerciseName, '%'))
        ORDER BY r.date DESC, t.name, s.setOrder
    """)
    List<ExerciseRecordResponseDto> searchRecords(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("exerciseName") String exerciseName,
            @Param("muscleGroup") String muscleGroup
    );
}