package com.example.fitnessWeb.repository;

import com.example.fitnessWeb.dto.ExerciseRecordResponseDto;
import com.example.fitnessWeb.entity.ExerciseRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface ExerciseRecordRepository extends JpaRepository<ExerciseRecord, Long> {
    @Query("""
    SELECT new com.example.fitnessWeb.dto.ExerciseRecordResponseDto(
        r.createdAt,
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
      AND (:startDate IS NULL OR r.createdAt >= :startDate)
      AND (:endDate IS NULL OR r.createdAt <= :endDate)
      AND (:muscleGroup IS NULL OR t.muscleGroup = :muscleGroup)
      AND (:exerciseName IS NULL OR t.name like concat('%', :exerciseName, '%'))
    ORDER BY r.createdAt DESC, t.name, s.setOrder
""")
    List<ExerciseRecordResponseDto> searchRecords(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("exerciseName") String exerciseName,
            @Param("muscleGroup") String muscleGroup
    );
}